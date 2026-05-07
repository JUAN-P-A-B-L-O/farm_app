package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.auth.service.EmailConfirmationSender;
import com.jpsoftware.farmapp.shared.exception.EmailDispatchException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
public class SmtpEmailConfirmationSender implements EmailConfirmationSender {

    private final EmailProperties emailProperties;
    private final SmtpTransport smtpTransport;

    public SmtpEmailConfirmationSender(EmailProperties emailProperties) {
        this(emailProperties, new SocketSmtpTransport());
    }

    public SmtpEmailConfirmationSender(EmailProperties emailProperties, SmtpTransport smtpTransport) {
        this.emailProperties = emailProperties;
        this.smtpTransport = smtpTransport;
    }

    @Override
    public void sendConfirmationEmail(String recipientEmail, String recipientName, String confirmationUrl) {
        try {
            smtpTransport.send(
                    emailProperties,
                    resolveFromAddress(),
                    recipientEmail.trim(),
                    buildMessage(recipientEmail, recipientName, confirmationUrl));
        } catch (IOException exception) {
            throw new EmailDispatchException("Unable to send confirmation email", exception);
        }
    }

    private String resolveFromAddress() {
        if (!StringUtils.hasText(emailProperties.getFrom())) {
            throw new IllegalStateException("app.email.from must be configured when app.email.enabled is true");
        }
        return emailProperties.getFrom().trim();
    }

    private String buildMessage(String recipientEmail, String recipientName, String confirmationUrl) {
        String body = buildBody(recipientName, confirmationUrl);
        return "From: <" + resolveFromAddress() + ">\r\n"
                + "To: <" + recipientEmail.trim() + ">\r\n"
                + "Subject: " + emailProperties.getConfirmation().getSubject().trim() + "\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/plain; charset=US-ASCII\r\n"
                + "Content-Transfer-Encoding: 7bit\r\n"
                + "\r\n"
                + escapeMessageBody(body);
    }

    private String buildBody(String recipientName, String confirmationUrl) {
        String resolvedName = resolveRecipientName(recipientName);
        return """
                Ola %s,

                Recebemos o cadastro da sua conta no Farm App.
                Confirme seu e-mail acessando o link abaixo:
                %s

                Se voce nao solicitou este cadastro, ignore esta mensagem.
                """.formatted(resolvedName, confirmationUrl);
    }

    private String resolveRecipientName(String recipientName) {
        if (!StringUtils.hasText(recipientName)) {
            return "usuario";
        }

        String trimmedName = recipientName.trim();
        return StandardCharsets.US_ASCII.newEncoder().canEncode(trimmedName) ? trimmedName : "usuario";
    }

    private String escapeMessageBody(String body) {
        String normalizedBody = body.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalizedBody.split("\n", -1);
        StringBuilder escapedBody = new StringBuilder();
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            if (line.startsWith(".")) {
                escapedBody.append('.');
            }
            escapedBody.append(line);
            if (index < lines.length - 1) {
                escapedBody.append("\r\n");
            }
        }
        return escapedBody.toString();
    }

    public interface SmtpTransport {

        void send(EmailProperties emailProperties, String fromAddress, String recipientEmail, String message)
                throws IOException;
    }

    private static final class SocketSmtpTransport implements SmtpTransport {

        @Override
        public void send(EmailProperties emailProperties, String fromAddress, String recipientEmail, String message)
                throws IOException {
            try (SmtpSession smtpSession = SmtpSession.open(emailProperties)) {
                smtpSession.expectCode(220, smtpSession.readResponse());
                smtpSession.sendEhlo();

                if (emailProperties.getSmtp().isStarttlsEnabled()
                        && !"smtps".equalsIgnoreCase(emailProperties.getSmtp().getProtocol())) {
                    smtpSession.startTls();
                    smtpSession.sendEhlo();
                }
                if (emailProperties.getSmtp().isAuth()) {
                    smtpSession.authenticate();
                }

                smtpSession.sendCommand("MAIL FROM:<" + fromAddress + ">", 250);
                smtpSession.sendCommand("RCPT TO:<" + recipientEmail + ">", 250);
                smtpSession.sendData(message);
                smtpSession.quit();
            }
        }
    }

    private static final class SmtpSession implements Closeable {

        private static final String CLIENT_NAME = "farmapp.local";

        private Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private final EmailProperties emailProperties;

        private SmtpSession(Socket socket, EmailProperties emailProperties) throws IOException {
            this.socket = socket;
            this.emailProperties = emailProperties;
            refreshStreams(socket);
        }

        static SmtpSession open(EmailProperties emailProperties) throws IOException {
            EmailProperties.Smtp smtp = emailProperties.getSmtp();
            if (!StringUtils.hasText(smtp.getHost())) {
                throw new IllegalStateException("app.email.smtp.host must be configured when app.email.enabled is true");
            }
            if (smtp.isAuth() && (!StringUtils.hasText(smtp.getUsername()) || !StringUtils.hasText(smtp.getPassword()))) {
                throw new IllegalStateException(
                        "app.email.smtp.username and app.email.smtp.password must be configured when app.email.smtp.auth is true");
            }

            Socket socket;
            if ("smtps".equalsIgnoreCase(smtp.getProtocol())) {
                socket = SSLSocketFactory.getDefault().createSocket();
            } else {
                socket = new Socket();
            }

            socket.connect(new InetSocketAddress(smtp.getHost().trim(), smtp.getPort()), smtp.getConnectionTimeoutMs());
            socket.setSoTimeout(smtp.getTimeoutMs());

            if (socket instanceof SSLSocket sslSocket) {
                sslSocket.startHandshake();
            }

            return new SmtpSession(socket, emailProperties);
        }

        void sendEhlo() throws IOException {
            sendCommand("EHLO " + CLIENT_NAME, 250);
        }

        void startTls() throws IOException {
            sendCommand("STARTTLS", 220);
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    socket,
                    emailProperties.getSmtp().getHost().trim(),
                    emailProperties.getSmtp().getPort(),
                    true);
            sslSocket.startHandshake();
            socket = sslSocket;
            refreshStreams(socket);
        }

        void authenticate() throws IOException {
            sendCommand("AUTH LOGIN", 334);
            sendCommand(base64(emailProperties.getSmtp().getUsername().trim()), 334);
            sendCommand(base64(emailProperties.getSmtp().getPassword()), 235);
        }

        void sendData(String message) throws IOException {
            sendCommand("DATA", 354);
            writer.write(message);
            writer.write("\r\n.\r\n");
            writer.flush();
            expectCode(250, readResponse());
        }

        void quit() throws IOException {
            sendCommand("QUIT", 221);
        }

        void sendCommand(String command, int expectedCode) throws IOException {
            writer.write(command);
            writer.write("\r\n");
            writer.flush();
            expectCode(expectedCode, readResponse());
        }

        String readResponse() throws IOException {
            StringBuilder response = new StringBuilder();
            String line;
            do {
                line = reader.readLine();
                if (line == null) {
                    throw new IOException("SMTP server closed the connection unexpectedly");
                }
                if (!response.isEmpty()) {
                    response.append('\n');
                }
                response.append(line);
            } while (line.length() >= 4 && line.charAt(3) == '-');
            return response.toString();
        }

        void expectCode(int expectedCode, String response) throws IOException {
            if (response.length() < 3) {
                throw new IOException("Invalid SMTP response: " + response);
            }
            int actualCode = Integer.parseInt(response.substring(0, 3));
            if (actualCode != expectedCode) {
                throw new IOException("Unexpected SMTP response (" + actualCode + "): " + response);
            }
        }

        private void refreshStreams(Socket activeSocket) throws IOException {
            reader = new BufferedReader(new InputStreamReader(activeSocket.getInputStream(), StandardCharsets.US_ASCII));
            writer = new BufferedWriter(new OutputStreamWriter(activeSocket.getOutputStream(), StandardCharsets.US_ASCII));
        }

        private String base64(String value) {
            return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.US_ASCII));
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
