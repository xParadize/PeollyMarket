//package com.peolly.utilservice;
//
//import com.peolly.utilservice.exceptions.IncorrectSearchPath;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
//
//@RestControllerAdvice
//@RequiredArgsConstructor
//public class ControllerAdvice {
//
//    @ExceptionHandler(IncorrectSearchPath.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ResponseEntity<ApiResponse> handleIncorrectSearchPath() {
//        ApiResponse response = new ApiResponse(false, "There's nothing here." +
//                "Try going back or looking for something else.");
//        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<ApiResponse> handleTypeMismatchException() {
//        ApiResponse response = new ApiResponse(false, "Invalid parameter type");
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<ApiResponse> handleIllegalArgumentException() {
//        ApiResponse response = new ApiResponse(false, "Invalid argument");
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(JwtTokenExpiredException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ResponseEntity<ApiResponse> handleJwtTokenExpiredException() {
//        ApiResponse response = new ApiResponse(false, "Authentication error. Please login again");
//        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//    }
//
//    @ExceptionHandler(AdminAccessOnlyException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ResponseEntity<ApiResponse> handleAdminAccessOnlyException() {
//        ApiResponse response = new ApiResponse(false, "The request is understood, but access is not allowed");
//        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//    }
//
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ResponseEntity<ApiResponse> handleException(Exception ex) {
//        ApiResponse response = new ApiResponse(false, "Internal Server Error");
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @ExceptionHandler(NoAuthenticationException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ResponseEntity<ApiResponse> handleNoAuthenticationException(NoAuthenticationException ex) {
//        ApiResponse response = new ApiResponse(false, "Unauthorized access - Please log in");
//        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//    }
//
//    @ExceptionHandler(NullPrincipalDataException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ResponseEntity<ApiResponse> handleNullPrincipalDataException(NullPrincipalDataException ex) {
//        ApiResponse response = new ApiResponse(false, "Unauthorized access - Please log in");
//        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//    }
//
//    @ExceptionHandler(OrganizationNotFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ResponseEntity<ApiResponse> handleOrganizationNotFoundException(OrganizationNotFoundException ex) {
//        ApiResponse response = new ApiResponse(false, "Organization not found");
//        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
//        ApiResponse response = new ApiResponse(false, "This is not the web page you are looking for");
//        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(UsernameNotFoundException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ResponseEntity<ApiResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
//        ApiResponse response = new ApiResponse(false, "Username not found or user not logged in");
//        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//    }
//
//    @ExceptionHandler(EmptyCartException.class)
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<ApiResponse> handleEmptyCartException(EmptyCartException ex) {
//        ApiResponse response = new ApiResponse(false, "Your cart is empty");
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    @ExceptionHandler(NoCreditCardLinkedException.class)
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<ApiResponse> handleNoCreditCardLinkedException(NoCreditCardLinkedException ex) {
//        ApiResponse response = new ApiResponse(false, "Payment methods are missing");
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    @ExceptionHandler(TicketNotFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ResponseEntity<ApiResponse> handleTicketNotFoundException(TicketNotFoundException ex) {
//        ApiResponse response = new ApiResponse(false, "Requested ticket not found");
//        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(NoUserTicketException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<ApiResponse> handleNoUserTicketException(NoUserTicketException ex) {
//        ApiResponse response = new ApiResponse(false, "You haven't created any ticket or you have already created organization");
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(UserAlreadyHasOrganizationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<ApiResponse> handleUserAlreadyHasOrganizationException(UserAlreadyHasOrganizationException ex) {
//        ApiResponse response = new ApiResponse(false, "It's forbidden to registry 2 and more organizations on 1 account");
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(UserAlreadyHasOrgTicketException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<ApiResponse> handleUserAlreadyHasOrgTicketException(UserAlreadyHasOrgTicketException ex) {
//        ApiResponse response = new ApiResponse(false, "You have already 1 ticket on review");
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(OrganizationHasNoProductsException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<ApiResponse> handleOrganizationHasNoProductsException(OrganizationHasNoProductsException ex) {
//        ApiResponse response = new ApiResponse(false, "This organization has no products");
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(NoNotificationsException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ResponseEntity<ApiResponse> handleNoNotificationsException(NoNotificationsException ex) {
//        ApiResponse response = new ApiResponse(false, "You haven't got any notifications now");
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//}
