package backend.academy.scrapper.service;

import backend.academy.scrapper.model.LinkUpdate;

public interface MessageTransport {
    void sendUpdate(LinkUpdate update);
} 