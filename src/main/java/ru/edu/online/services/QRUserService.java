package ru.edu.online.services;

import ru.edu.online.entities.QRUser;

public interface QRUserService {

    public String getContentWithHash(QRUser qrUser);
    //  получение полной инфы о пользователе для авторизованного сканирующего
    String getFullStaticQRPayload(QRUser qrUser);
    //  получение урезанной инфы о пользователе для НЕавторизованного сканирующего
    String getAbbreviatedStaticQRPayload(QRUser qrUser);
    // получить хэш для пользователя
    String getHash(QRUser qrUser);

}
