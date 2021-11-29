package ru.edu.online.services;

import ru.edu.online.entities.interfaces.QRUser;

public interface QRUserService {

    //  получение полной инфы о пользователе для авторизованного сканирующего
    String getFullStaticQRPayload(QRUser qrUser);
    //  получение урезанной инфы о пользователе для НЕавторизованного сканирующего
    String getAbbreviatedStaticQRPayload(QRUser qrUser);
    // получить хэш для пользователя
    String getHash(QRUser qrUser);

}
