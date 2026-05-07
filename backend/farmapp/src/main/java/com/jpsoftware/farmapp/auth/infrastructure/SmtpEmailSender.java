package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

    private final EmailProperties emailProperties;
    private final SmtpTransport smtpTransport;

    @Autowired
    public SmtpEmailSender(EmailProperties emailProperties) {
        this(emailProperties, new SocketSmtpTransport());
    }

    public SmtpEmailSender(EmailProperties emailProperties, SmtpTransport smtpTransport) {
        this.emailProperties = emailProperties;
        this.smtpTransport = smtpTransport;
    }

    @Override
    public void send(EmailMessage emailMessage) {
        validateEmailMessage(emailMessage);
        try {
            smtpTransport.send(
                    emailProperties,
                    resolveFromAddress(),
                    emailMessage.recipientEmail().trim(),
                    buildMessage(emailMessage));
        } catch (IOException exception) {
            throw new EmailDispatchException("Unable to send email", exception);
        }
    }

    private void validateEmailMessage(EmailMessage emailMessage) {
        if (emailMessage == null) {
            throw new IllegalArgumentException("emailMessage must not be null");
        }
        if (!StringUtils.hasText(emailMessage.recipientEmail())) {
            throw new IllegalArgumentException("recipientEmail must not be blank");
        }
        if (!StringUtils.hasText(emailMessage.subject())) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (!StringUtils.hasText(emailMessage.body())) {
            throw new IllegalArgumentException("body must not be blank");
        }
    }

    private String resolveFromAddress() {
        if (!StringUtils.hasText(emailProperties.getFrom())) {
            throw new IllegalStateException("app.email.from must be configured when app.email.enabled is true");
        }
        return emailProperties.getFrom().trim();
    }

    private String buildMessage(EmailMessage emailMessage) {
        return "From: <" + resolveFromAddress() + ">\r\n"
                + "To: <" + emailMessage.recipientEmail().trim() + ">\r\n"
                + "Subject: " + encodeHeader(emailMessage.subject().trim()) + "\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "\r\n"
                + escapeMessageBody(encodeBody(emailMessage.body()));
    }

    private String encodeHeader(String headerValue) {
        String encoded = Base64.getEncoder().encodeToString(headerValue.getBytes(StandardCharsets.UTF_8));
        return "=?UTF-8?B?" + encoded + "?=";
    }

    private String encodeBody(String body) {
        byte[] bodyBytes = body.replace("\r\n", "\n").replace('\r', '\n').getBytes(StandardCharsets.UTF_8);
        return Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(bodyBytes);
    }

    private String escapeMessageBody(String body) {
        String[] lines = body.split("\r\n", -1);
        StringBuilder escapedBody = new StringBuilder(body.length() + 16);
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
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("SMTP server closed the connection unexpectedly");
            }

            StringBuilder response = new StringBuilder(line);
            while (line.length() > 3 && line.charAt(3) == '-') {
                line = reader.readLine();
                if (line == null) {
                    throw new IOException("SMTP server closed the connection unexpectedly");
                }
                response.append("\n").append(line);
            }
            return response.toString();
        }

        void expectCode(int expectedCode, String response) throws IOException {
            if (response.length() < 3) {
                throw new IOException("Invalid SMTP response: " + response);
            }
            int responseCode;
            try {
                responseCode = Integer.parseInt(response.substring(0, 3));
            } catch (NumberFormatException exception) {
                throw new IOException("Invalid SMTP response: " + response, exception);
            }
            if (responseCode != expectedCode) {
                throw new IOException("Unexpected SMTP response " + responseCode + ": " + response);
            }
        }

        private void refreshStreams(Socket socket) throws IOException {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }

        private String base64(String value) {
            return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        }
    }
}
