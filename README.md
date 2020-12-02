# My Places
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.20-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Save your favourite places in an easy to use app based on modern Android application tech-stacks and MVVM architecture.

## Features
* Add a place with title, description, date, location and image.
* Select an image from phone gallery or take a picture with the camera.
* Add any location or set current location based on phone GPS.
* View info of a saved place.
* See your saved location on Google Maps.
* Swipe to edit or delete a place.
* Quickly search your saved places by title.
* Option to delete all saved places.
* Spanish translation.

<img src="images/app.gif" align="right" width="28%"/>

## Tech Stack & Open-source libraries
* Minimum SDK level 21.
* [Kotlin](https://kotlinlang.org/) - official programming language for Android development.
* [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - for asynchronous programming.
* [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - collection of libraries that help you design robust, testable, and maintainable apps.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - data objects that notify views when the underlying database changes.
  - [Data Binding](https://developer.android.com/topic/libraries/data-binding) - data objects to bind UI components in your layouts to data sources in your app.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - stores UI-related data that isn't destroyed on UI changes. 
  - [Room](https://developer.android.com/topic/libraries/architecture/room) - access your app's SQLite database with in-app objects and compile-time checks.
  - [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) - components to perform actions in response to a change in the lifecycle status of another component, such as activities and fragments.
* [Navigation](https://developer.android.com/guide/navigation) - interactions that allow users to navigate across, into, and back out from the different pieces of content within your app.
* [Fragment](https://developer.android.com/guide/components/fragments) - represents a behavior or a portion of user interface in a FragmentActivity.
* [RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview) - for display a scrolling list of elements based on large data sets.
  - [RecyclerView Animators](https://github.com/wasabeef/recyclerview-animators) - library that allows developers to easily create RecyclerView with animations.
* [Material-Components](https://github.com/material-components/material-components-android) - Material design components.
  - [Material Dialogs](https://github.com/afollestad/material-dialogs) - library with extensible dialogs API for Kotlin & Android.
  - [Circle Image View](https://) - A fast circular ImageView.
* [Dexter](https://github.com/Karumi/Dexter) - Library that simplifies the process of requesting permissions at runtime.
* [Places SDK](https://developers.google.com/places/android-sdk/overview) - Build location-aware apps that respond contextually to the local businesses and other places near the user's device.
* [Play Services]()

## Build
In order to build and use the app you need to add the following string resource on the "strings.xml" file with your Google Maps API Key. In order to get a Key see the following documentation [Maps SDK for Android ](https://developers.google.com/maps/documentation/android-sdk/start)

```xml
<string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR API KEY</string>
```

## Architecture
This app uses [MVVM (Model View View-Model)](https://developer.android.com/jetpack/docs/guide#recommended-app-arch) architecture.

<img src="images/architecture.png" width="70%"/>

## Contribute
Awesome! If you would like to contribute you're always welcome!

# License
```xml
Copyright 2020 Anibal Ventura

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
