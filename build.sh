#! /bin/bash

#java -jar /Users/zy/Documents/GitHub/exam-clock-2020/build/libs/packr.jar /Users/zy/Documents/GitHub/exam-clock-2020/build/libs/packr-config-win.json
java -jar /Users/zy/Documents/GitHub/exam-clock-2020/build/libs/packr.jar /Users/zy/Documents/GitHub/exam-clock-2020/build/libs/packr-config-mac.json
cd ./build/deploy/
#zip -vr exam-clock-win.zip win -x "*.DS_Store"
zip -vr exam-clock-osx.zip ExamClock.app -x "*.DS_Store"