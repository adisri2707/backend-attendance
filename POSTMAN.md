# Local Postman testing

The default configuration starts without Firebase authentication so you can test
the API before the Firebase project is configured. Ensure MySQL is running, set
`DB_PASSWORD` if your local root password is not `root`, then run
`./mvnw spring-boot:run`. Use `http://localhost:8080` as the base URL.

Create records in this order:

1. `POST /api/roles`

   ```json
   {"roleName":"ADMIN","description":"Administrator"}
   ```

2. `POST /employees`

   ```json
   {"firstName":"Aditi","lastName":"Sharma","email":"aditi@example.com","phoneNumber":"9876543210","department":"HR","designation":"Manager","dateOfJoining":"2026-08-05"}
   ```

3. `POST /api/users`

   ```json
   {"username":"aditi","password":"Pass@123","roleId":1,"employeeId":1,"active":true}
   ```

4. `POST /api/attendance`

   ```json
   {"employeeId":1,"attendanceDate":"2026-08-05","checkInTime":"09:00:00","checkOutTime":"18:00:00","status":"PRESENT","remarks":""}
   ```

For Firebase-protected mode, set `APP_SECURITY_ENABLED=true`,
`FIREBASE_ENABLED=true`, and `FIREBASE_CREDENTIALS_PATH` to an absolute path to
a service-account JSON file downloaded from Firebase Console. Send the Firebase
ID token from your client as `Authorization: Bearer <id-token>`.
