LIBRARY MANAGEMENT SYSTEM - JAVA AND SQLITE

FEATURES
1. Admin login
2. Student login and student self-registration
3. Admin can add books, search/view books, and delete books
4. Students can browse available books and issue books
5. Students can view and return their active loans
6. SQLite database stores users, books and loan records locally
7. Book availability is reduced on issue and increased on return
8. Fine calculation: 14-day loan period, then Rs. 2 per overdue day

DEMO LOGIN
Admin:
  Username: admin
  Password: admin123

Student:
  Username: student
  Password: student123

Students can also register from the login screen.

BUILD APK WITHOUT INSTALLING ANDROID STUDIO
This project includes a GitHub Actions workflow.
1. Download and extract this ZIP.
2. Create a GitHub repository.
3. Upload all files and folders from this project to the repository root.
   Include the hidden .github folder.
4. Open the repository's Actions tab.
5. Enable Actions if GitHub asks.
6. Open "Build Library Management APK" and choose Run workflow, or push/commit the files to main to trigger it.
7. Wait for a green check mark.
8. Open the completed workflow run and download the artifact named LibraryManagement-debug-apk.
9. Extract the artifact ZIP to get app-debug.apk.

IMPORTANT NOTES
- This is a student/demo project, not a production-ready library system.
- Demo passwords are stored as plain text for simplicity. Do not use real passwords or real personal data.
- The fine is calculated and displayed at return; payment collection is not integrated.
- Data is stored on the device in SQLite and is not synchronized across devices.
- The source ZIP has not been compiled or tested in an Android SDK environment here. The GitHub Actions workflow is provided to attempt the build.
