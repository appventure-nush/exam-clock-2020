## Contributing
If you wish to contribute to exam clock, you have two choices, issues and pull requests

### Issues <sub>[New](https://github.com/appventure-nush/exam-clock-2020/issues/new)</sub>
Make a issue if you encounter a problem, a bug, or you have some suggestions

### Pull request
If you wish to contribute directly, i.e. providing the code, you can fork this project, and create a pull request

### Testing

1. Change `jdkVersion` variable in `build.gradle` to `1.8` if it is not already `1.8`
2. Build project
3. Run task 'shadowJar'
4. Repeat 1-3 but with `jdkVersion` as `1.14` or withever newer version of java you have installed
5. You should see 2 new jars under jar output folder
6. Run both jars in their respective java versions
7. Open settings, connection and add exam window on both versions
8. Run some simple manual testing
9. If everything works fine, you can start writing your pull request
