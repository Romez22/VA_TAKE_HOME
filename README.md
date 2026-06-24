Virgin Active Take home summary

Running the apps
Android
  1. Open the project in Android Studio.
  2. Pick an emulator (any recent virtual device) from the device dropdown at the top.
  3. Press Run (the green).
  The one thing to know: an Android emulator can't see your Mac's localhost directly — to the  emulator, the host machine is a special address (10.0.2.2). The app is already wired to use that, so you don't configure anything; it just reaches the mock on your machine automatically. Then sign in with one of the test users.
  iOS
  1. Open the Xcode project (iosApp/iosApp/iosApp.xcodeproj) in Xcode.
  2. Pick an iPhone simulator — on your Apple-Silicon Mac, any iPhone simulator is fine (it runs the arm64 build, which is the only simulator slice the shared framework ships).
  3. Press Run .
A couple of things happen for you behind the scenes when you hit
Run: a build step automatically rebuilds and embeds the shared Kotlin framework into the app, and the iOS simulator can see your Mac's localhost directly (unlike Android), so it talks to the mock with no address translation. Then sign in with a test user.
  The mental model
  - Mock on :8080 = your local backend, in-memory, single-client.
  - Android emulator reaches it via the host-loopback alias; iOS simulator reaches it via plain localhost. Both are pre-wired.
 - Sign in with a provided test user, and you're into the live app:
personalised Home, weekly timetable, book/waitlist a class.

Design
I have implemented Clean Architecture as my design pattern. This allows me to separate core business rules from UI and platform specific code. What I think is cool about this approach is that since core business rules are shared, the UI becomes flexible, meaning we can easily switch out native SwiftUI and Compose for Compose-multiplatform or add a web app using the same business rules. One of the trade offs are a lot of boilerplate because every feature requires a useCase, interface and implementation, custom data models for domain layer and code to map data from network layer into domain models and then into UI state models. This might be overkill for a take home assessment but in production the benefits outweigh the trade offs and I wanted to demonstrate this. 
The mock api does not return as the swagger indicates it would, one example of this would be the “classes” property in the timetable response, it does not show the structure and this could only be determined by making a request to the endpoint. The home screen is server driven and intentionally sends junk you don’t recognize named “experimental”. The classID from the server also return ‘::’ which is a special character that needs to be encoded exactly once when going into a URL.

AI usage
While writing the code, I used AI autocomplete features on both android studio and Xcode to help me scaffold objects and functions quickly. I also used Claude to assist with planning the project, as well as helping with writing and understanding some iOS coding and concepts. 
I rejected the AI idea of sharing ViewModels across platforms, mainly because the scope of the assessment is not that big and most if not all business logic lies within the use cases and implementations.
I accepted the AI recommended phases in which to build the project, starting with the shared logic and then  moving unto android to get a working app end to end before building the iOS UI. I also used AI to generate the placeholder images.
With more time I would add tests using AI, and ask AI to do a code review.



