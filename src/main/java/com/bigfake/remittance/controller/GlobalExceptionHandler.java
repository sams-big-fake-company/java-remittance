package com.bigfake.remittance.controller;
import com.bigfake.remittance.exception.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
import java.util.*;
@ControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(NotFoundException.class) ResponseEntity<Map<String,String>> notFound(NotFoundException e){return response(HttpStatus.NOT_FOUND,e.getMessage());}
 @ExceptionHandler(ConflictException.class) ResponseEntity<Map<String,String>> conflict(ConflictException e){return response(HttpStatus.CONFLICT,e.getMessage());}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<Map<String,String>> bad(IllegalArgumentException e){return response(HttpStatus.BAD_REQUEST,e.getMessage());}
 private ResponseEntity<Map<String,String>> response(HttpStatus status,String message){Map<String,String> body=new HashMap<>();body.put("error",message);return ResponseEntity.status(status).body(body);}
}
