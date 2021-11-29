package ru.edu.online.services;

import ru.edu.online.entities.QRUser;
import ru.edu.online.entities.dto.PermanentUserQRDTO;

public interface QRUserService {

    public String getContentWithHash(QRUser qrUser);
    //  получение полной инфы о пользователе для авторизованного сканирующего
    PermanentUserQRDTO getFullStaticQRPayload(QRUser qrUser);
    //  получение урезанной инфы о пользователе для НЕавторизованного сканирующего
    PermanentUserQRDTO getAbbreviatedStaticQRPayload(QRUser qrUser);
    // получить хэш для пользователя
    String getHash(QRUser qrUser);

}
