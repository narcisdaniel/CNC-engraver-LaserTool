# CNC-engraver-LaserTool
The theme of this project is the development of a simple and intuitive laser engraving system, the system being composed of a two axis CNC that uses a laser module as a machining tool and a mobile application used for model processing and CNC control.
The mobile application was named LaserTool and it was developed in the Android Studio IDE. The goal of the application is to process the models, send them to the CNC and the CNC control. It is composed of 4 screens (views), each with a well-defined role, the idea behind this division being to create an environment as simple, friendly and intuitive as possible. In the mobile application, a filter is applied to the uploaded model, it’s color palette being restricted to two colors (black & white). The application also offers an option for changing the resolution of the model, the possibility of saving it in the phone’s gallery, but also features necessary to the engraving process (sending the model to the CNC, testing the dimensions of the surface to be engraved, starting the engraving process). To allow the use of the filter even if the two components are not connected, an operating mode dedicated to this aspect has been implemented.
The whole ensemble is a final product, its operation being based on the communication between the two components described.
