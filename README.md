# Keep clone

![Notes page screenshot](https://github.com/rafaelklaessen/keep-clone/raw/master/screenshots/notes.jpg "Notes page")

An effort to make a clone of [Google Keep](https://keep.google.com) in play-scala with Firebase.
It has:

- A login system that saves user data to Firebase
- Log in with Google/GitHub/Facebook
- Note creating/deleting/updating with Firebase
- Note sharing; notes can be shared with other users
- Note pinning and archiving
- A settings page in which a user can edit his/her settings and delete his/her account

## About
The project's main focus is to learn Scala/Play and Firebase.

## Setup

#### Requirements to run the project locally:
- Activator 1.3.12 including Play 2.5.12 (requires JDK 1.8)
- NodeJS with Babel

#### Setup instructions
- Clone the repo
- Cd into the repo folder and run `activator ui`
- In Activator, go to the run tab and run the project after it's compiled
- The project now runs at [localhost:9000](http://localhost:9000)
- To compile the JavaScript, cd into the public folder and run `babel es6 --watch --out-file javascripts/main.js`
- You're all set! You should be able to register an account at [localhost:9000/register](http://localhost:9000/register) and login at [localhost:9000/login](http://localhost:9000/login)

#### Make sign in with Facebook & GitHub work
To make sign in with Facebook & GitHub work, you'll need a client_id and client_secret for both of them.
You'll have to go into `app/controllers/OAuthController.scala` and put them in the right place (GitHub is around line 60, Facebook is around line 102).

## Run the project in production
To run the project in production, you'll have to do the following:
- Cd into the project
- First compile the JavaScript: cd into the public folder and run `babel es6 --watch --out-file javascripts/main.js`
- Cd back to the project folder
- Type `sbt` to enter the sbt console
- Type `playGenerateSecret` to generate an app secret. You'll need it to run the project.
- Type `dist` to package the project. After it's done, the package is in `target/universal/keep-clone-1.0.zip`
- Extract the zip file to any location you want
- In the extracted folder, go into `conf/application.conf` and edit the `firebaseAuthPath` variable to the Firebase authentication file path
- In the terminal, type `keep-clone-1.0/bin/keep-clone -Dplay.crypto.secret=YOUR_APP_SECRET`
- The project now runs at [localhost:9000](http://localhost:9000)
