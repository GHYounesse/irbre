package com.irbre.collector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Task 1
class User{
    private Long id;
    private String name;
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

public class UserService {
    private List<User> users;
    public void addUser(User user){
        users.add(user);
    }
    public Optional<User> findByEmail(String email){
        return Optional.ofNullable(users.stream().filter(user -> user.getEmail().equals(email)).findFirst().orElse(null));
    }
    public List<User> getAll(){
        return users;
    }
}
//Task 2

class UserRequestDTO{
    private String email;
    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
class UserResponseDTO{
    private String email;
    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


@RestController
@RequestMapping("/users")
class UserController{
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    public UserController(UserService userService){
        this.userService = userService;
    }
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserRequestDTO request){
        try {
            User user=userMapper.toEntity(request);
            userService.addUser(user);
            return ResponseEntity.ok("User added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @GetMapping
    public ResponseEntity<List<User>> getAll(){
        List<User> users =userService.getAll();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/search")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@RequestParam String email){
        User user=userService.findByEmail(email).get();

        if(user==null){
            return ResponseEntity.notFound().build();
        }
        UserResponseDTO response=userMapper.toDto(user);
        return ResponseEntity.ok(response);
    }
}

//Task 3
public class UserService {
    private final ConcurrentHashMap<String, User> usersByEmail = new ConcurrentHashMap<>();

    public void addUser(User user) {
        boolean exists= usersByEmail.putIfAbsent(user.getEmail(), user)!=null;
        if(exists){
            throw new IllegalArgumentException("User already exists");
        }
    }

    public Optional<User> findByEmail(String email) {
        User user=usersByEmail.get(email);
        return Optional.ofNullable(user);
    }

    public List<User> getAll() {
        return usersByEmail.values().stream().collect(Collectors.toList());
    }
}
// Task 4
record Product(String id,String name,
        double price){}
class ProductService {

    public List<Product> filterExpensive(List<Product> products) {
        return products.stream()
                .filter(p -> p.price() > 100)
                .sorted(Comparator.comparing(Product::price).reversed())
                .toList();
    }
}



class NotFoundException extends RuntimeException {
    public NotFoundException(){}
}
@RestControllerAdvice
class SExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> notFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


}
record Book(Long id,String name,String author){ }

class BookService{
    List<Book> books= new ArrayList<>();
    public Book findById(String id){
        Book book=books.stream().filter(b -> b.id().equals(id) ).findFirst().orElseThrow(() -> new NotFoundException());
        return book;
    }

}

import java.util.Scanner;

public class Solution {

    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        System.out.println("================================");
        for(int i=0;i<3;i++){
            String s1=sc.next();
            int x=sc.nextInt();
            int d = String.valueOf(x).length();


            StringBuilder sb = new StringBuilder();

            sb.append(s1);

            for (int y = 0; y < 15 - s1.length(); y++) {
                sb.append(' ');
            }

            for (int y = 0; y < 3-d ; y++) {
                sb.append('0');
            }
            sb.append(x);
            System.out.println(sb.toString());
        }
        System.out.println("================================");

    }
}



}