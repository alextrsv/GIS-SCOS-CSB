//package ru.edu.online.services.Impl;
//
//import org.springframework.stereotype.Service;
//import ru.edu.online.clients.GisScosApiRestClient;
//import ru.edu.online.clients.VamRestClient;
//import ru.edu.online.services.IDynamicQRUserService;
//import ru.edu.online.services.IPassRequestService;
//import ru.edu.online.services.QRUserService;
//
//@Service
//public abstract class QRUserServiceImpl implements QRUserService {
//
//    protected final IPassRequestService passRequestService;
//
//    protected final GisScosApiRestClient gisScosApiRestClient;
//
//    protected final VamRestClient vamRestClient;
//
//    protected final IDynamicQRUserService dynamicQRUserService;
//
//    public QRUserServiceImpl(IPassRequestService passRequestService, GisScosApiRestClient gisScosApiRestClient, VamRestClient vamRestClient, IDynamicQRUserService dynamicQRUserService) {
//        this.passRequestService = passRequestService;
//        this.gisScosApiRestClient = gisScosApiRestClient;
//        this.vamRestClient = vamRestClient;
//        this.dynamicQRUserService = dynamicQRUserService;
//    }
//
//
//    public IPassRequestService getPassRequestService() {
//        return passRequestService;
//    }
//
//    public GisScosApiRestClient getGisScosApiRestClient() {
//        return gisScosApiRestClient;
//    }
//
//    public VamRestClient getVamRestClient() {
//        return vamRestClient;
//    }
//
//    public IDynamicQRUserService getDynamicQRUserService() {
//        return dynamicQRUserService;
//    }
//}
