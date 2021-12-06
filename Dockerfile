FROM openjdk:11
EXPOSE 8081
ADD target/order_service.jar order_service.jar
ENTRYPOINT ["java", "-jar", "/order_service.jar"]