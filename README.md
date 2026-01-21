# YearMatrix

**YearMatrix** is a minimalist Android application and **Live Wallpaper Service** designed to provide a visual representation of time through a data-driven interface. Built with **Kotlin** and the **Android Canvas API**, it transforms the standard home screen into a functional dashboard that tracks your progress through the day, week, and year.

---

## 🚀 Key Features

* **365-Dot Matrix**: A visual grid where each dot represents one day of the year. The grid fills dynamically based on the current date, with built-in logic to handle leap years.
* **24-Hour Progress Bar**: A vertical loading bar on the left that tracks the current day in minutes ($1440$ minutes per day), updating every 60 seconds for precision.
* **7-Day Week Progress Bar**: A vertical bar on the right that indicates the current day of the week, from Sunday to Saturday.
* **Live Wallpaper Engine**: A dedicated `WallpaperService` that renders the UI directly onto your background, optimized for efficiency.
* **Deep Personalization**:
    * **Custom Backgrounds**: Set your own image from the gallery as the backdrop for the matrix.
    * **Dot Color Picker**: Choose from a variety of colors to match your home screen aesthetic.
    * **UI Alignment**: Adjust the matrix positioning (Left, Center, Right or Top, Center, Bottom) to fit around your icons.

---

## 🛠️ Technical Stack

* **Language**: Kotlin.
* **Graphics**: Android Canvas & Custom View drawing using `RectF`, `Paint`, and `Path`.
* **Architecture**: `WallpaperService` with a `SurfaceHolder` for persistent background rendering.
* **Persistence**: `SharedPreferences` to ensure your color and alignment preferences persist after reboot.
* **Time API**: Extensive use of `java.util.Calendar` for precise time-to-coordinate mapping.

---

## 📊 Data Visualization Logic

As a project developed by a **CSE Data Science** student at **AVIT**, **YearMatrix** focuses on accurate data mapping:

1.  **Normalization**: All time values are converted to normalized floats ($0.0$ to $1.0$) to ensure smooth rendering across different screen resolutions.
    $$Progress_{Day} = \frac{(CurrentHour \times 60) + CurrentMinute}{1440}$$
2.  **Efficient Rendering**: The wallpaper engine utilizes a `Handler` loop that refreshes only when necessary, minimizing CPU cycles and preserving battery life.
3.  **Coordinate Calculation**: The 19x19 dot grid is calculated dynamically based on screen dimensions to maintain a perfect aspect ratio regardless of device orientation.

---

## 📦 Getting Started

1.  **Clone** this repository to your local machine.
2.  Open the project in **Android Studio**.
3.  **Build and Run** the app on your Android device.
4.  **Set Wallpaper**: Long-press your home screen > **Wallpapers** > **Live Wallpapers** > select **YearMatrix**.

---

## 🎓 Academic Context

This project was developed as part of a professional portfolio focused on engineering and data-driven application development.

* **Institution**: **Aarupadai Veedu Institute of Technology (AVIT)**
* **Major**: **B.E. in CSE Data Science**

---
