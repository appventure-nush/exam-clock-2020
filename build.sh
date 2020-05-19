#! /bin/bash

java -jar /Users/zy/Documents/GitHub/exam-clock-2020/build/libs/packr.jar /Users/zy/Documents/GitHub/exam-clock-2020/build/libs/packr-config-win.json
java -jar /Users/zy/Documents/GitHub/exam-clock-2020/build/libs/packr.jar /Users/zy/Documents/GitHub/exam-clock-2020/build/libs/packr-config-mac.json
zip -r /Users/zy/Documents/GitHub/exam-clock-2020/build/deploy/ExamClock-win.zip /Users/zy/Documents/GitHub/exam-clock-2020/build/deploy/win/
zip -r /Users/zy/Documents/GitHub/exam-clock-2020/build/deploy/ExamClock-osx.zip /Users/zy/Documents/GitHub/exam-clock-2020/build/deploy/ExamClock.app/