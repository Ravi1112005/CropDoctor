# CropDoctor

CropDoctor is an Android application designed to help farmers and gardeners quickly identify plant diseases using image recognition. By simply taking a picture of a diseased leaf, users can get an instant diagnosis, detailed descriptions of the disease, recommended treatments, and prevention strategies. The app also keeps a history of all diagnoses for easy reference.

## ✨ Features

Here's a quick tour of CropDoctor's key features, presented with detailed descriptions and illustrative screenshots:

| Feature                   | Screenshots                                                                                                       |
| :------------------------ | :---------------------------------------------------------------------------------------------------------------- |
| **User Authentication** <br> _Securely log in or create a new account using email/password or conveniently with your Google account._ | **Login Screen:** <br> <img src="screenshots/login.jpg" alt="Login Screen" width="200" height="360"/> &nbsp; **Register Screen:** <br> <img src="screenshots/sign-up.jpg" alt="Register Screen" width="200" height="360"/> |
| **Plant Disease Diagnosis** <br> _Effortlessly identify plant diseases by taking a photo of a diseased leaf. Get instant results and key information._ | **Dashboard:** <br> <img src="screenshots/home.jpg" alt="Dashboard Screen" width="200" height="360"/> &nbsp; **Scan Crop Screen:** <br> <img src="screenshots/scan_crop.jpg" alt="Diagnosis Screen" width="200" height="360"/> |
| **Detailed Diagnosis Results** <br> _Receive comprehensive details about the detected disease, including symptoms, type, treatment, and prevention strategies._ | **Result (Cedar Apple Rust):** <br> <img src="screenshots/result1.jpg" alt="Result Screen 1" width="220" height="400"/> &nbsp; **Result (Early Blight):** <br> <img src="screenshots/result2.jpg" alt="Result Screen 2" width="220" height="400"/> |
| **Diagnosis History** <br> _Access a chronological record of all your past diagnoses, allowing you to review previous scans and their results._     | **History Screen:** <br> <img src="screenshots/history.jpg" alt="History Screen" width="200" height="360"/>        |
| **User Profile Management** <br> _Manage your personal information and account settings, ensuring your app experience is tailored to you._ | **Profile Screen:** <br> <img src="screenshots/profile.jpg" alt="Profile Screen" width="200" height="360"/>        |
| **Settings** <br> _Customize various app preferences, such as theme settings, and manage local data like cache._              | **Settings Screen:** <br> <img src="screenshots/settings.jpg" alt="Settings Screen" width="200" height="360"/>      |
| **Navigation Drawer** <br> _Navigate seamlessly through different sections of the app with an intuitive and accessible side menu._     | **Navigation Drawer Open:** <br> <img src="screenshots/drawer.jpg" alt="Navigation Drawer" width="220" height="400"/> |

## 🚀 Technology Stack

*   **Jetpack Compose:** For building the declarative UI.
*   **Gemini API:** Utilized for advanced image analysis and accurate disease identification.
*   **Firebase (Firestore, Authentication, Storage):** Provides robust backend services for user management, secure authentication, and data storage.
*   **Coil:** An efficient image loading and caching library for Android.
*   **Navigation Compose:** For seamless in-app navigation management.
