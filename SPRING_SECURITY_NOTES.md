# Spring Security in SmartShop - Complete Guide for Beginners

## Table of Contents
1. [What is Spring Security?](#what-is-spring-security)
2. [The Big Picture](#the-big-picture)
3. [Step-by-Step Implementation](#step-by-step-implementation)
4. [How Everything Works Together](#how-everything-works-together)

---

## What is Spring Security?

Imagine your house has a security system. Spring Security is like that, but for your web application!

**Think of it like this:**
- Your house = Your application
- Front door lock = Login system
- Security cameras = Monitoring who does what
- Room keys = Different permissions (some people can enter bedroom, some can't)
- Security guard = Spring Security checking everyone

---

## The Big Picture

### The Security Flow (Like a School Security System)

```
1. Student arrives at school gate (User visits website)
   ↓
2. Shows ID card (Sends username & password)
   ↓
3. Security guard checks ID (Spring Security validates)
   ↓
4. Gets a visitor badge (Receives JWT token)
   ↓
5. Uses badge to enter different rooms (Access different pages)
   ↓
6. Some rooms need special permission (Admin areas)
```

---

## Step-by-Step Implementation

### STEP 1: Adding Security Tools (Dependencies)

**File: `pom.xml`**

Think of this like buying security equipment for your house.

```xml
<!-- The main security system -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Google login feature (like "Sign in with Google") -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- JWT tokens (like digital badges) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
```

**Simple Explanation:**
- `spring-boot-starter-security` = The security guard
- `spring-boot-starter-oauth2-client` = Allows Google to verify users
- `jjwt-api` = Creates special digital badges (tokens)

---

### STEP 2: Creating User Accounts (User Entity)

**File: `User.java`**

This is like creating a student ID card with all information.

```java
@Entity
public class User implements UserDetails {
    private Long id;              // Student number
    private String firstName;     // First name
    private String lastName;      // Last name
    private String email;         // Email (used as username)
    private String password;      // Secret password (encrypted)
    private UserRole role;        // Type: CUSTOMER, ADMIN, VENDOR, STAFF
}
```

**Key Points:**
- `implements UserDetails` = Tells Spring Security "this is a user account"
- `UserRole` = Like different colored badges (blue=customer, red=admin)
- Password is NEVER stored as plain text (it's scrambled/encrypted)

**The 4 User Types:**
1. **CUSTOMER** - Regular shoppers (can buy products)
2. **ADMIN** - Boss (can do everything)
3. **VENDOR** - Sellers (can add products)
4. **STAFF** - Helpers (can manage orders)

---

### STEP 3: Password Encryption (PasswordEncoder)

**File: `SecurityConfig.java`**

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Simple Explanation:**

Imagine you write a secret message:
- Original: "mypassword123"
- Encrypted: "$2a$10$xYz...AbC" (looks like gibberish)

**Why?**
If a hacker steals the database, they see gibberish, not real passwords!

**How it works:**
1. User registers with password "hello123"
2. BCrypt scrambles it → "$2a$10$abc..."
3. Stored in database as scrambled version
4. When user logs in with "hello123"
5. BCrypt scrambles it again and compares
6. If they match → Login successful!

---

### STEP 4: JWT Tokens (Digital Badges)

**File: `JwtService.java`**

**What is JWT?**
JWT = JSON Web Token = A digital badge that proves who you are

**Think of it like a movie ticket:**
- Has your name
- Has expiration time
- Has special code that can't be faked
- Shows what you can access

**Example JWT Token:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGVtYWlsLmNvbSIsInJvbGVzIjpbIlJPTEVfQ1VTVE9NRVIiXX0.abc123xyz
```

**Parts of JWT:**
1. **Header** (type of token)
2. **Payload** (user info: email, role)
3. **Signature** (secret code to verify it's real)

**Two Types of Tokens:**

1. **Access Token** (Short-lived - 15 minutes)
   - Like a temporary pass
   - Used for every request
   - Expires quickly for security

2. **Refresh Token** (Long-lived - 7 days)
   - Like a renewal card
   - Used to get new access token
   - Lasts longer

**Key Methods:**

```java
// Creates a new access token
public String generateAccessToken(UserDetails userDetails) {
    return Jwts.builder()
        .subject(userDetails.getUsername())      // Who is this?
        .claim("roles", roles)                   // What can they do?
        .issuedAt(new Date())                    // When created?
        .expiration(new Date(...))               // When expires?
        .signWith(signingKey)                    // Secret signature
        .compact();
}

// Checks if token is valid
public boolean isTokenValid(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) 
        && !isTokenExpired(token)
        && !tokenBlacklistService.isRevoked(token);
}
```

---

### STEP 5: Login & Registration (AuthService)

**File: `AuthService.java`**

This is like the registration desk at school.

#### Registration Process:

```java
public AuthResponse register(AuthRegisterRequest request) {
    // 1. Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new Exception("Email already used!");
    }
    
    // 2. Create new user
    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))  // Encrypt!
        .role(request.getRole())
        .build();
    
    // 3. Save to database
    userRepository.save(user);
    
    // 4. Give them tokens (badges)
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    
    // 5. Return tokens
    return new AuthResponse(accessToken, refreshToken, ...);
}
```

**Step-by-step:**
1. User fills registration form
2. Check if email is already taken
3. Encrypt the password
4. Save user to database
5. Create JWT tokens
6. Send tokens back to user

#### Login Process:

```java
public AuthResponse login(AuthLoginRequest request) {
    // 1. Try to authenticate
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    
    // 2. If successful, find user
    User user = userRepository.findByEmail(request.getEmail());
    
    // 3. Generate new tokens
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    
    // 4. Return tokens
    return new AuthResponse(accessToken, refreshToken, ...);
}
```

**Step-by-step:**
1. User enters email and password
2. Spring Security checks if they match
3. If correct, find user details
4. Create new JWT tokens
5. Send tokens to user

---

### STEP 6: JWT Authentication Filter

**File: `JwtAuthenticationFilter.java`**

This is like a security guard at every door checking badges.

**What it does:**
Every time someone makes a request, this filter checks their badge (JWT token).

```java
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
    
    // 1. Get the Authorization header
    String authHeader = request.getHeader("Authorization");
    
    // 2. Check if it starts with "Bearer "
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);  // No token, continue
        return;
    }
    
    // 3. Extract the token
    String jwt = authHeader.substring(7);  // Remove "Bearer "
    
    // 4. Get username from token
    String username = jwtService.extractUsername(jwt);
    
    // 5. Load user details
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    
    // 6. Validate token
    if (jwtService.isTokenValid(jwt, userDetails)) {
        // 7. Set authentication in Spring Security
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
    
    // 8. Continue to next filter
    filterChain.doFilter(request, response);
}
```

**Flow:**
```
Request comes in
    ↓
Check for "Authorization: Bearer <token>" header
    ↓
Extract token
    ↓
Verify token is valid
    ↓
Load user information
    ↓
Set user as authenticated
    ↓
Allow request to continue
```

---

### STEP 7: Security Configuration (Main Setup)

**File: `SecurityConfig.java`**

This is the master control panel for all security settings.

#### Key Configurations:

**1. CSRF Protection**

```java
.csrf(AbstractHttpConfigurer::disable)
```

**What is CSRF?**
CSRF = Cross-Site Request Forgery = A type of attack

**Simple Example:**
- You're logged into your bank
- You visit a bad website
- Bad website tricks your browser to transfer money
- Bank thinks it's you because you're logged in

**Why disabled here?**
- We use JWT tokens in headers (not cookies)
- CSRF attacks work with cookies
- JWT in headers = safe from CSRF

**2. Session Management**

```java
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

**What does STATELESS mean?**

**Traditional way (STATEFUL):**
- You login
- Server remembers you (stores session)
- Like teacher remembering who's present

**JWT way (STATELESS):**
- You login
- Get a token
- Every request includes token
- Server doesn't remember you
- Like showing ID card every time

**Why STATELESS?**
- Faster (no session storage)
- Scalable (works with multiple servers)
- Modern approach

**3. URL Authorization Rules**

```java
.authorizeHttpRequests(auth -> auth
    // Anyone can access these (no login needed)
    .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
    
    // Anyone can view products (GET only)
    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
    
    // Only ADMIN can access admin pages
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    
    // ADMIN and STAFF can access staff pages
    .requestMatchers("/api/staff/**").hasAnyRole("ADMIN", "STAFF")
    
    // Everything else needs login
    .anyRequest().authenticated()
)
```

**Think of it like school zones:**
- Playground = Everyone (permitAll)
- Classroom = Students only (authenticated)
- Teacher's lounge = Teachers only (hasRole("TEACHER"))
- Principal's office = Principal only (hasRole("PRINCIPAL"))

---

### STEP 8: Custom User Details Service

**File: `CustomUserDetailsService.java`**

This loads user information from the database.

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) {
        // Find user by email in database
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
```

**What it does:**
1. Spring Security asks: "Who is john@email.com?"
2. This service looks in database
3. Returns user information
4. Spring Security uses it to check permissions

---

### STEP 9: Token Blacklist (Logout System)

**File: `TokenBlacklistService.java`**

**Problem:** JWT tokens can't be "deleted" because they're self-contained.

**Solution:** Keep a list of "banned" tokens (blacklist).

```java
@Service
public class TokenBlacklistService {
    
    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();
    
    // Add token to blacklist
    public void revokeToken(String token, long expiryMs) {
        revokedTokens.put(token, expiryMs);
    }
    
    // Check if token is blacklisted
    public boolean isRevoked(String token) {
        return revokedTokens.containsKey(token);
    }
    
    // Clean up expired tokens every 30 minutes
    @Scheduled(fixedRate = 1800000)
    public void cleanupExpiredTokens() {
        // Remove old tokens to save memory
    }
}
```

**How logout works:**
1. User clicks "Logout"
2. Their token is added to blacklist
3. Next time they use that token
4. System checks blacklist
5. Token is rejected
6. User must login again

---

### STEP 10: OAuth2 Login (Google Sign-In)

**Files: `CustomOAuth2UserService.java` and `OAuth2LoginSuccessHandler.java`**

This allows users to login with their Google account.

#### How it works:

**1. User clicks "Sign in with Google"**

**2. Redirected to Google**
```
User → Your App → Google Login Page
```

**3. User logs in with Google**

**4. Google sends user info back**
```java
public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    
    // Get user info from Google
    String email = oAuth2User.getAttribute("email");
    String firstName = oAuth2User.getAttribute("given_name");
    String lastName = oAuth2User.getAttribute("family_name");
    
    // Check if user exists in our database
    User user = userRepository.findByEmail(email)
        .orElseGet(() -> {
            // Create new user if doesn't exist
            User newUser = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(UserRole.CUSTOMER)
                .oauth2Provider("google")
                .build();
            return userRepository.save(newUser);
        });
    
    return oAuth2User;
}
```

**5. Success handler creates JWT tokens**
```java
public void onAuthenticationSuccess(Authentication authentication) {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");
    
    User user = userRepository.findByEmail(email);
    
    // Create JWT tokens
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    
    // Redirect to frontend with tokens
    String redirectUrl = "http://localhost:3000/login?token=" + accessToken;
    response.sendRedirect(redirectUrl);
}
```

**6. User is logged in!**

---

### STEP 11: Error Handlers

#### CustomAuthenticationEntryPoint (401 Unauthorized)

**File: `CustomAuthenticationEntryPoint.java`**

Handles when someone tries to access protected pages without login.

```java
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) {
        
        // Return JSON error instead of redirect
        response.setStatus(401);
        response.setContentType("application/json");
        
        ApiResponse<Void> error = new ApiResponse<>(
            401,
            "Authentication required. Please login.",
            null
        );
        
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
```

**When it triggers:**
- User not logged in
- Token expired
- Token invalid

**Response:**
```json
{
  "status": 401,
  "message": "Authentication required. Please login.",
  "data": null
}
```

#### CustomAccessDeniedHandler (403 Forbidden)

**File: `CustomAccessDeniedHandler.java`**

Handles when logged-in user tries to access pages they don't have permission for.

```java
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) {
        
        response.setStatus(403);
        response.setContentType("application/json");
        
        ApiResponse<Void> error = new ApiResponse<>(
            403,
            "Access denied: insufficient permissions",
            null
        );
        
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
```

**When it triggers:**
- CUSTOMER tries to access /api/admin/**
- VENDOR tries to access admin-only features

**Response:**
```json
{
  "status": 403,
  "message": "Access denied: insufficient permissions",
  "data": null
}
```

---

### STEP 12: Role-Based Access Control

**File: `RequiresRole.java` and `RoleInterceptor.java`**

Custom annotation to restrict access by role.

**Creating the annotation:**
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    UserRole[] value();
}
```

**Using it in controllers:**
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    // Anyone can view products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAll();
    }
    
    // Only ADMIN and VENDOR can add products
    @PostMapping
    @RequiresRole({UserRole.ADMIN, UserRole.VENDOR})
    public Product addProduct(@RequestBody ProductDTO dto) {
        return productService.create(dto);
    }
    
    // Only ADMIN can delete products
    @DeleteMapping("/{id}")
    @RequiresRole(UserRole.ADMIN)
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }
}
```

**The interceptor checks roles:**
```java
@Component
public class RoleInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) {
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresRole annotation = handlerMethod.getMethodAnnotation(RequiresRole.class);
        
        if (annotation == null) {
            return true;  // No role required
        }
        
        String userRole = (String) request.getAttribute("authenticatedUserRole");
        
        // Check if user has required role
        for (UserRole requiredRole : annotation.value()) {
            if (requiredRole.name().equals(userRole)) {
                return true;  // Access granted
            }
        }
        
        throw new UnauthorizedException("Insufficient permissions");
    }
}
```

---

## How Everything Works Together

### Complete Request Flow

Let's follow a request from start to finish:

#### Scenario: Customer wants to view their orders

**1. User makes request:**
```http
GET /api/orders
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**2. JwtAuthenticationFilter intercepts:**
```
→ Extract token from header
→ Validate token signature
→ Check if token expired
→ Check if token blacklisted
→ Extract username from token
→ Load user from database
→ Set authentication in SecurityContext
```

**3. SecurityConfig checks authorization:**
```
→ Is /api/orders in permitAll list? NO
→ Does it require specific role? NO
→ Does it require authentication? YES
→ Is user authenticated? YES (from step 2)
→ Allow request to continue
```

**4. RoleInterceptor checks (if @RequiresRole present):**
```
→ Does method have @RequiresRole? Check annotation
→ What role does user have? CUSTOMER
→ Is CUSTOMER in allowed roles? Check
→ Grant or deny access
```

**5. Controller processes request:**
```java
@GetMapping
public List<Order> getMyOrders() {
    User currentUser = getCurrentUser();
    return orderService.findByUserId(currentUser.getId());
}
```

**6. Response sent back:**
```json
{
  "status": 200,
  "message": "Orders retrieved successfully",
  "data": [...]
}
```

---

### Security Layers (Defense in Depth)

Think of security like layers of an onion:

**Layer 1: HTTPS**
- Encrypts data in transit
- Prevents eavesdropping

**Layer 2: CORS**
- Controls which websites can call your API
- Prevents unauthorized domains

**Layer 3: JWT Authentication Filter**
- Validates tokens
- Identifies users

**Layer 4: SecurityConfig Authorization**
- URL-based access control
- Role-based restrictions

**Layer 5: Method-level Security**
- @PreAuthorize annotations
- @RequiresRole custom annotations

**Layer 6: Business Logic**
- Additional checks in service layer
- Data ownership validation

---

### Configuration Files

**application-dev.properties:**
```properties
# Database connection
spring.datasource.url=${APP_DB_URL}
spring.datasource.username=${APP_DB_USER}
spring.datasource.password=${APP_DB_PASSWORD}

# JWT settings
jwt.secret=${APP_JWT_SECRET}
jwt.access-token-expiration-ms=900000        # 15 minutes
jwt.refresh-token-expiration-ms=604800000    # 7 days

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=${APP_GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${APP_GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=email,profile

# Redirect after OAuth2 login
oauth2.redirect-uri=${APP_REDIRECT_URL}
```

**Environment Variables (.env file):**
```
APP_DB_URL=jdbc:postgresql://localhost:5432/smartshop
APP_DB_USER=postgres
APP_DB_PASSWORD=yourpassword
APP_JWT_SECRET=your-256-bit-secret-key-here
APP_GOOGLE_CLIENT_ID=your-google-client-id
APP_GOOGLE_CLIENT_SECRET=your-google-client-secret
APP_REDIRECT_URL=http://localhost:3000/login
```

---

## Common Security Patterns Used

### 1. Password Hashing
- Never store plain passwords
- Use BCrypt (one-way encryption)
- Can't reverse engineer original password

### 2. JWT Tokens
- Stateless authentication
- Self-contained (includes user info)
- Signed to prevent tampering

### 3. Token Refresh
- Short-lived access tokens (15 min)
- Long-lived refresh tokens (7 days)
- Reduces risk if token stolen

### 4. Token Blacklist
- Allows logout functionality
- Revokes compromised tokens
- Auto-cleanup prevents memory leaks

### 5. Role-Based Access Control (RBAC)
- Users have roles
- Roles have permissions
- Easy to manage access

### 6. OAuth2 Integration
- Delegate authentication to Google
- Don't store Google passwords
- Easier for users

### 7. CORS Configuration
- Whitelist allowed origins
- Prevents unauthorized API access
- Protects against cross-origin attacks

### 8. Custom Error Handlers
- Consistent error responses
- Proper HTTP status codes
- Helpful error messages

---

## Security Best Practices in This Project

✅ **Passwords encrypted** with BCrypt
✅ **JWT tokens** for stateless auth
✅ **Token expiration** (15 min access, 7 day refresh)
✅ **Token blacklist** for logout
✅ **Role-based access** control
✅ **OAuth2** integration (Google)
✅ **CORS** configured
✅ **CSRF** disabled (appropriate for JWT)
✅ **Custom error handlers**
✅ **Logging** security events
✅ **Environment variables** for secrets
✅ **HTTPS** ready (in production)

---

## Testing the Security

### 1. Register a new user:
```bash
POST /api/auth/register
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@email.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

### 2. Login:
```bash
POST /api/auth/login
{
  "email": "john@email.com",
  "password": "password123"
}
```

Response:
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "john@email.com",
  "role": "CUSTOMER"
}
```

### 3. Use token in requests:
```bash
GET /api/orders
Authorization: Bearer eyJhbGci...
```

### 4. Try accessing admin endpoint (should fail):
```bash
GET /api/admin/users
Authorization: Bearer eyJhbGci...
```

Response:
```json
{
  "status": 403,
  "message": "Access denied: insufficient permissions"
}
```

### 5. Logout:
```bash
POST /api/auth/logout
Authorization: Bearer eyJhbGci...
```

### 6. Try using same token (should fail):
```bash
GET /api/orders
Authorization: Bearer eyJhbGci...
```

Response:
```json
{
  "status": 401,
  "message": "Authentication required. Please login."
}
```

---

## Summary

Spring Security in this project provides:

1. **Authentication** - Proving who you are (login)
2. **Authorization** - What you're allowed to do (permissions)
3. **Password Security** - Encrypted storage
4. **Token Management** - JWT creation and validation
5. **Session Management** - Stateless approach
6. **OAuth2 Integration** - Google sign-in
7. **Role-Based Access** - Different user types
8. **Logout Functionality** - Token blacklist
9. **Error Handling** - Proper responses
10. **Security Logging** - Audit trail

All working together to keep the application secure! 🔒
