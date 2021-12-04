//package com.streets.ordersvc.service;
//
//import com.google.rpc.Code;
//import com.streets.order.*;
//import com.streets.ordersvc.common.types.Tuple2;
//import com.streets.ordersvc.dao.models.Leg;
//import com.streets.ordersvc.enums.OrderStatus;
//import com.streets.ordersvc.enums.Side;
//import com.streets.ordersvc.processing.scan.PriceQuantityScanningService;
//import com.streets.ordersvc.processing.scan.ScanResult;
//import com.streets.ordersvc.validation.services.ValidationServiceImpl;
//import io.grpc.protobuf.StatusProto;
//import io.grpc.stub.StreamObserver;
//import net.devh.boot.grpc.server.service.GrpcService;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@GrpcService
//public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {
//    private final String[] xs = {"EXCHANGE1", "EXCHANGE2"};
//
//    @Autowired
//    private final ValidationServiceImpl validationService;
//
//    @Autowired
//    private final PriceQuantityScanningService priceScanner;
//
//    public OrderServiceImpl(ValidationServiceImpl validationService, PriceQuantityScanningService priceQuantityScanningService) {
//        this.validationService = validationService;
//        this.priceScanner = priceQuantityScanningService;
//    }
//
//    /**
//     * @param request
//     * @param responseObserver
//     */
//    @Override
//    public void placeOrder(OrderRequest request, StreamObserver<Order> responseObserver) {
//        if (request.getUserId().equals("")) {
//            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
//                    .setCode(Code.INVALID_ARGUMENT.getNumber())
//                    .setMessage("User ID was not provided")
//                    .build();
//            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
//            responseObserver.onCompleted();
//        }
//        String userId = request.getUserId();
//        com.streets.ordersvc.dao.models.Order d = new com.streets.ordersvc.dao.models.Order();
//
//        d.setQuantity(request.getQuantity());
//        d.setPrice(request.getPrice());
//        d.setClientId(request.getUserId());
//        d.setSide(request.getSide());
//
//        // TODO: no short selling for now
//        d.setIsShort(false);
//
//        // TODO: make a request to the market data service to get the current market price
////        d.setMarketPrice();
//        d.setStatus(OrderStatus.UNPROCESSED);
//
//        // validate amount
//        Tuple2<Boolean, String> amountValidationResult = validationService.isValidAmount(d);
//        if (!amountValidationResult.getIsValid()) {
//            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
//                    .setCode(Code.FAILED_PRECONDITION.getNumber())
//                    .setMessage(amountValidationResult.getMsg())
//                    .build();
//            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
//            responseObserver.onCompleted();
//        }
//
//        // validate quantity
//        Tuple2<Boolean, String> quantityValidationResult = validationService.isValidQuantity(d);
//        if (!quantityValidationResult.getIsValid()) {
//            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
//                    .setCode(Code.FAILED_PRECONDITION.getNumber())
//                    .setMessage(quantityValidationResult.getMsg())
//                    .build();
//            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
//            responseObserver.onCompleted();
//        }
//
//        // validate rate
//        Tuple2<Boolean, String> rateValidationResult = validationService.isValidRate(d);
//        if (!rateValidationResult.getIsValid()) {
//            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
//                    .setCode(Code.FAILED_PRECONDITION.getNumber())
//                    .setMessage(rateValidationResult.getMsg())
//                    .build();
//            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
//            responseObserver.onCompleted();
//        }
//
//        Side side = Side.valueOf(request.getSide());
//
//        Integer totalQuantity = request.getQuantity();
//
//        // go scan the order book and return the result
//        List<ScanResult> results = priceScanner.scanBook(xs, request.getProduct(), side);
//        if (side == Side.BUY) {
//            // sort in ascending order
//            results.sort(Comparator.comparingDouble(ScanResult::getMinPrice));
//        } else {
//            // sort in descending order
//            results.sort(Comparator.comparingDouble(ScanResult::getMinPrice).reversed());
//
//        }
//        Set<Leg> orderLegs = new HashSet<>();
//        // split till quantity is fulfilled
//        for (ScanResult result : results) {
//            if (totalQuantity > 0) {
//                Leg leg = new Leg();
//                leg.setProduct(d.getProduct());
//                leg.setSide(d.getSide());
//                leg.setQuantity(Math.min(result.getQuantity(), totalQuantity));
//                totalQuantity -= leg.getQuantity();
//                leg.setOrder(d);
//                orderLegs.add(leg);
//            }
//
//        }
//        d.setLegs(orderLegs);
//        // now the order is valid, so we go ahead and place the order
//    }
//
//    /**
//     * @param request
//     * @param responseObserver
//     */
//    @Override
//    public void cancelOrder(CancelOrderRequest request, StreamObserver<Order> responseObserver) {
//        super.cancelOrder(request, responseObserver);
//    }
//
//    /**
//     * @param request
//     * @param responseObserver
//     */
//    @Override
//    public void editOrder(EditOrderRequest request, StreamObserver<Order> responseObserver) {
//        super.editOrder(request, responseObserver);
//    }
//
//    /**
//     * @param request
//     * @param responseObserver
//     */
//    @Override
//    public void listOrders(ListOrdersRequest request, StreamObserver<OrderList> responseObserver) {
//        super.listOrders(request, responseObserver);
//    }
//}
