# CropDoctor

CropDoctor is an Android application designed to help farmers and gardeners quickly identify plant diseases using image recognition. By simply taking a picture of a diseased leaf, users can get an instant diagnosis, detailed descriptions of the disease, recommended treatments, and prevention strategies. The app also keeps a history of all diagnoses for easy reference.

## Features

*   **User Authentication:** Secure login and registration with email/password or Google Sign-In.
    *   **Login Screen:**
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Flogin.png?alt=media&token=16460613-2d25-4c6e-8120-d12f4623a9b1" alt="Login Screen" width="250"/>
    *   **Register Screen:**
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Fsign_up.png?alt=media&token=24c87c80-6927-466a-b286-9040000a6390" alt="Register Screen" width="250"/>

*   **Plant Disease Diagnosis:** Analyze plant images to detect diseases.
    *   **Dashboard:** Quickly access the scan feature and view recent diagnoses.
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Fdashboard.png?alt=media&token=c1901a1c-34d1-4e78-9993-4a11c1d80b7e" alt="Dashboard Screen" width="250"/>
    *   **Scan Crop Screen:** Capture images of plant leaves for analysis.
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Fdiagnosis.png?alt=media&token=c9676e2c-e129-4d6a-939e-d3f3f0846df0" alt="Diagnosis Screen" width="250"/>

*   **Detailed Diagnosis Results:** View comprehensive information about the detected disease.
    *   **Result Screen (Cedar Apple Rust):**
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Fresult_1.png?alt=media&token=5b27457c-2b5d-4001-9c60-a29d519d08e5" alt="Result Screen 1" width="250"/>
    *   **Result Screen (Late Blight):**
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Fresult_2.png?alt=media&token=1801c876-0f79-460d-9861-f09b30c51126" alt="Result Screen 2" width="250"/>

*   **Diagnosis History:** Keep track of all previous diagnoses with images and details.
    *   **History Screen:**
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Fhistory.png?alt=media&token=a860ee83-b924-41d3-a077-440266ef637e" alt="History Screen" width="250"/>

*   **User Profile Management:** View and update user information.
    *   **Profile Screen:**
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Fprofile.png?alt=media&token=143ee62d-0b19-4b68-8094-1a91e576ecae" alt="Profile Screen" width="250"/>

*   **Settings:** Customize app preferences, including theme.
    *   **Settings Screen:**
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Fsettings.png?alt=media&token=30b8e23b-0937-418f-a957-802c63810a9f" alt="Settings Screen" width="250"/>

*   **Navigation Drawer:** Easy access to various sections of the app.
    *   **Navigation Drawer Open:**
        <img src="https://firebasestorage.googleapis.com/v0/b/generative-ai-client-sdk.appspot.com/o/sdk-assets%2Fdrawer.png?alt=media&token=fdd6a57c-b715-46aa-8367-933390c23945" alt="Navigation Drawer" width="250"/>

## Technology Stack

*   **Jetpack Compose:** For building the UI.
*   **Gemini API:** For advanced image analysis and disease identification.
*   **Firebase (Firestore, Authentication, Storage):** For backend services, user management, and data storage.
*   **Coil:** For image loading and caching.
*   **Navigation Compose:** For managing in-app navigation.
