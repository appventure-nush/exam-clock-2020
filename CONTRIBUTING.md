# Exam Clock

The exam clock is the big clock in front of everyone in an exam, or in any situation the teacher wants.

#### Features

- Shows time
  - **Please make a better looking clock**
- Shows events/exams
  - And their start/end
  - And their duration/progress
  - Highly customizable display/layout
  - **Please make the display look nicer**
- Show toilet icons (F/M)
  - My graphics design is bad
  - Remote controllable
  - **Please design better icons/effects**
- Remote Control <small>***SUWARU SUWARU OH SIT DOWN PLEASE YEAH!***</small>
  - Access the exam clock from anywhere around the world
  - Add exams, delete exams, or edit exams
  - Start all exams, stop all exams etc
  - Updates are realtime both ways (bit buggy as in not tested vigorously)
  - **Please add more privacy and security stuff here**
- Export / Import / Save / Load
  - As it says, saves all exams in **json** format
  - Save/Load is from memory and done automatically (last session)
  - Export/Import can be used to export/import
  - **Please make this more user friendly**

# How to contribute

#### Exam Clock (Gradle)

- Learn Java + JavaFX

- Use **IntelliJ IDEA** (free student liscense) for better integration

  - Note that the project uses Java 14 to profile however Gradle will compile the project in both **Java 14** and **Java 8**, so install both
  - To change gradle's compile version (useful for jar export), open `build.gradle` and edit `double jdkVersion = x.xx` to `1.14` or `1.8`

- Learn basic Git (we are using Github)

- Steps to contribute

  1. `git clone https://github.com/appventure-nush/exam-clock-2020.git`

     Or use your whatever git service to clone https://github.com/appventure-nush/exam-clock-2020

  2. Open it as an intellij project

  3. Contribute

  4. Done

- Every class should have good Java Doc

  - I think I added java doc for every class and method
  - It will help others understand the function and requirement of your code

- Remember to change version number in `build.gradle`

##### Class Structure
![yes](https://user-images.githubusercontent.com/26460801/114972526-a0de6580-9eb9-11eb-805b-c00ab4091d9e.png)

#### Web panel (node.js/express backend, pure html/js frontend, no framework used)

- Learn Javascript + HTML + how to use node.js
- The web server is on `server` branch of the same repository
- Remember not to commit to wrong repository
- **DONT FORCE PUSH**

#### Why you should contribute

Everyone taking their exams (including you) will be looking at the clock and say:

> Wow, that guy who made the clock must be a nice person, such ingenuity, such cool, such... such clock

My classmate even called me last night on the phone and said:

> Bruh I think your clock is rigged, after I contributed an issue to it, my crush suddenly started messaging me

Developing the exam clock takes no time from your life at all

> What do you mean no time, you are just sitting on your chair all day looking at code, you are slacking aren't you

#### Issues <sub>[New](https://github.com/appventure-nush/exam-clock-2020/issues/new)</sub>
Make a issue if you encounter a problem, a bug, or you have some suggestions

#### Testing

1. Change `jdkVersion` variable in `build.gradle` to `1.8` if it is not already `1.8`
2. Build project
3. Run task 'shadowJar'
4. Repeat 1-3 but with `jdkVersion` as `1.14` or withever newer version of java you have installed
5. You should see 2 new jars under jar output folder
6. Run both jars in their respective java versions
7. Open settings, connection and add exam window on both versions
8. Run some simple manual testing
9. If everything works fine, you can start writing your pull request
