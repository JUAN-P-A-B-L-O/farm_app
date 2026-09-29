package com.jpsoftware.farmapp.shared.email.service;

import com.jpsoftware.farmapp.shared.email.model.EmailMessage;

public interface EmailSender {

    void send(EmailMessage emailMessage);
}
