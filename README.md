# Local Gallery Server (Spring version)

Backend server written in Java/Kotlin with Spring Boot for [Local Gallery Android app](https://github.com/memostark/LocalGallery) that provides child folders and images of selected directory.

A version written in Python can be found at this [repo](https://github.com/memostark/LocalGalleryServer).

# Run using source code
To run this program using only the source code follow the next steps:
- Make sure you have an IDE with gradle. Recommended to use [IntelliJ IDEA](https://www.jetbrains.com/es-es/idea/download/).
- Download the repo source code with `git clone https://github.com/memostark/LocalGalleryServerSpring`
- In the source code, go to the file `application.properties` which is located in `src/main/kotlin/com/guillermonegrete/gallery`. Put the path of the folder with your files in `base.path=your_path_here` replace "your_path_here" with your actual path e.g. `base.path=D:/files/`
- Run the program.
- You should be good to go, if you have your app setup you should be able to see folders with the images inside.