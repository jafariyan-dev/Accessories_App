# Accessories App

A simple Android app (Kotlin) for managing accessories. This project uses a local MySQL database accessed through a WAMP (Windows Apache MySQL PHP) backend — the Android client talks to PHP endpoints served by WAMP.

## Quick points
- Language: Kotlin (primary)
- Backend: PHP + MySQL (run under WAMP)
- Local dev: Android Studio + WAMP on the same machine (or any PHP/MySQL stack)

## Prerequisites
- Android Studio (with Android SDK)
- JDK 11+
- WAMP (Apache + MySQL + PHP) running on your development machine
- A device or emulator

## Setup (fast)
1. Clone the repo:
   git clone https://github.com/jafariyan-dev/Accessories_App.git

2. Prepare the backend (WAMP):
   - Place PHP backend files into WAMP's www folder (for example: C:\wamp64\www\Accessories_api\).
   - Create the MySQL database and import any provided SQL schema (or create the tables expected by the app).
   - Update DB credentials in the PHP files as needed.

3. Configure the app to reach the backend:
   - For the Android emulator use: http://10.0.2.2/Accessories_api/
   - For a real device use your PC's local IP: http://<YOUR-PC-IP>/Accessories_api/
   - Open the app code and update the backend/base URL (search for `BASE_URL`, `SERVER_URL` or `API` in the project).

4. Build & Run:
   - Open the project in Android Studio and run.
   - Or use Gradle wrapper:
     - Build: ./gradlew :app:assembleDebug
     - Install: ./gradlew :app:installDebug

## Troubleshooting
- Emulator can't reach backend? Use 10.0.2.2 instead of localhost.
- Ensure WAMP Apache and MySQL services are running and firewall allows connections.
- If endpoints fail, check PHP error logs and verify DB credentials.

## Contributing
Fork → branch → PR. Keep changes small and document any backend contract changes (endpoints / request/response).

## License
No license file included. Add a LICENSE if you want this repo to be open-source.

Contact: https://github.com/jafariyan-dev
