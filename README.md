# CropDoctor

CropDoctor is an Android application designed to help farmers and gardeners quickly identify plant diseases using image recognition. By simply taking a picture of a diseased leaf, users can get an instant diagnosis, detailed descriptions of the disease, recommended treatments, and prevention strategies. The app also keeps a history of all diagnoses for easy reference.

## Features

*   **User Authentication:** Secure login and registration with email/password or Google Sign-In.
    *   **Login Screen:**
        <img src="screenshots/login.jpg" alt="Login Screen" width="250" height="450"/>
    *   **Register Screen:**
        <img src="screenshots/sign-up.jpg" alt="Register Screen" width="250" height="450"/>

*   **Plant Disease Diagnosis:** Analyze plant images to detect diseases.
    *   **Dashboard:** Quickly access the scan feature and view recent diagnoses.
        <img src="screenshots/home.jpg" alt="Dashboard Screen" width="250" height="450"/>
    *   **Scan Crop Screen:** Capture images of plant leaves for analysis.
        <img src="screenshots/scan_crop.jpg" alt="Diagnosis Screen" width="250" height="450"/>

*   **Detailed Diagnosis Results:** View comprehensive information about the detected disease.
    *   **Result Screen (Cedar Apple Rust):**
        <img src="screenshots/result1.jpg" alt="Result Screen 1" width="280" height="500"/>
    *   **Result Screen (Early Blight):**
        <img src="screenshots/result2.jpg" alt="Result Screen 2" width="280" height="500"/>

*   **Diagnosis History:** Keep track of all previous diagnoses with images and details.
    *   **History Screen:**
        <img src="screenshots/history.jpg" alt="History Screen" width="250" height="450"/>

*   **User Profile Management:** View and update user information.
    *   **Profile Screen:**
        <img src="screenshots/profile.jpg" alt="Profile Screen" width="250" height="450"/>

*   **Settings:** Customize app preferences, including theme.
    *   **Settings Screen:**
        <img src="screenshots/settings.jpg" alt="Settings Screen" width="250" height="450"/>

*   **Navigation Drawer:** Easy access to various sections of the app.
    *   **Navigation Drawer Open:**
        <img src="screenshots/drawer.jpg" alt="Navigation Drawer" width="280" height="500"/>

## Technology Stack

*   **Jetpack Compose:** For building the UI.
*   **Gemini API:** For advanced image analysis and disease identification.
*   **Firebase (Firestore, Authentication, Storage):** For backend services, user management, and data storage.
*   **Coil:** For image loading and caching.
*   **Navigation Compose:** For managing in-app navigation.
