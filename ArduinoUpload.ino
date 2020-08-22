#include <SD.h>
#include <SPI.h>
#include <EEPROM.h>
#define EN        8  

//Direction pin
#define X_DIR     5 
#define Y_DIR     6


//Step pin
#define X_STP     2
#define Y_STP     3 

int delayTime=300; 
int stps=167;// Steps to move
int LSR=11;
File myFile;
int pinCS = 53;
char incomingByte;
String buffer;
int width,height;
void setup() {
  
  Serial.begin(9600);
  Serial1.begin(9600);
  pinMode(pinCS, OUTPUT);
 
  if (SD.begin())
  {
    Serial.println("SD card Up");
  } else
  {
    Serial.println("SD card failed");
    return;
  }
  pinMode(X_DIR, OUTPUT); 
  pinMode(X_STP, OUTPUT);
  pinMode(Y_DIR, OUTPUT); 
  pinMode(Y_STP, OUTPUT);
  pinMode(11, OUTPUT);
  analogWrite(11, 0);
  pinMode(EN, OUTPUT);
  digitalWrite(EN, HIGH);
  width=getEEP(0);
  height=getEEP(2);
}
void writeEEP(int address, int number)
{ 
  byte byte1 = number >> 8;
  byte byte2 = number & 0xFF;
  EEPROM.write(address, byte1);
  EEPROM.write(address + 1, byte2);
}
int getEEP(int address)
{
  byte byte1 = EEPROM.read(address);
  byte byte2 = EEPROM.read(address + 1);
  return (byte1 << 8) + byte2;
}
void step(boolean dir, byte dirPin, byte stepperPin, int steps)
{

  digitalWrite(dirPin, dir);

  for (int i = 0; i < steps; i++) {

    digitalWrite(stepperPin, HIGH);
    delayMicroseconds(delayTime); 
    digitalWrite(stepperPin, LOW);
    delayMicroseconds(delayTime); 

  }

}
bool checkPhoto(){
  myFile=SD.open("photo.txt",FILE_READ);
  if(width*height==myFile.size()){
      myFile.close();
      return true;
  }
  myFile.close();
  return false;  
}
void getPixels(){
myFile=SD.open("photo.txt", FILE_WRITE);
  while(true){
 
      if(Serial1.available()){
          incomingByte = Serial1.read();
          if(incomingByte==';'){
              myFile.close();
              
              if(checkPhoto()){
                  writeEEP(0,width);
                  writeEEP(2,height);
                  Serial1.println("W");
                  Serial.print("Good");
                  
              }
              else{
                  Serial1.println("Q");
                  Serial.print("Bad");
                  
              }
              break;
          }
          if(incomingByte!='\n'){
              //myFile=SD.open("photo.txt", FILE_WRITE);
              myFile.print(incomingByte);
              //Serial.print(incomingByte);
              
              //Serial.println(myFile.size());
              
             
          }
          
      }
  }
}
void readPhoto(){
  int ok=0;
  SD.remove("photo.txt");
  
   while(true) 
   {
        if(Serial1.available()){
           incomingByte = Serial1.read();
           
            if(incomingByte!='\n'){
               if(incomingByte!=';'){
                   buffer+=incomingByte;
                 
               }
               else{
                   if(ok==1){
                        height=buffer.toInt();
                        buffer="";
                        Serial.println(height);
                        getPixels();
                        break;
                   }
                   if(ok==0){
                      width=buffer.toInt();
                      buffer="";
                      ok++;
                      Serial.println(width);
                   }
                   
              }
           }       
        }
  }
}

void startTest(){
  if(checkPhoto()){
    Serial1.println("T");
    digitalWrite(EN, LOW);
    analogWrite(11, 7);
    for(int i=0;i<width;i++)
       step(false,X_DIR,X_STP,stps);
    for(int i=0;i<height;i++)
        step(true,Y_DIR,Y_STP,stps);
   for(int i=0;i<width;i++)
       step(true,X_DIR,X_STP,stps);
   for(int i=0;i<height;i++)
       step(false,Y_DIR,Y_STP,stps);
   digitalWrite(EN, HIGH);
   analogWrite(11, 0);
  }
  else{
    Serial1.println("Q");
  }
}
void startEngraving(){
    if(checkPhoto()){
     Serial1.println("E");
     digitalWrite(EN, LOW);
     myFile=SD.open("photo.txt",FILE_READ);
     for(int i=0;i<height;i++){
          for(int j=0;j<width;j++){
             if(myFile.read()=='1'){
                 Serial.print("ON ");
                 analogWrite(11, 250);
             }
             if(i%2)
                 step(true,X_DIR,X_STP,stps);
             else
                step(false,X_DIR,X_STP,stps);
            analogWrite(11, 0);
            Serial.print("OFF ");
         }
         delay(100);
         step(true,Y_DIR,Y_STP,stps);
         delay(100);
     }
      myFile.close();
      digitalWrite(EN, HIGH);
    }
   else{
    Serial1.println("Q");
  }
}
void read(){
   if (Serial1.available()) 
   {
        incomingByte = Serial1.read();
        Serial.print(incomingByte);
        if(incomingByte=='R')
            readPhoto();
        if(incomingByte=='S')
            readSettings();
        if(incomingByte=='T')
            startTest();
        if(incomingByte=='E')
            startEngraving();
        
               
  }
}

void loop() {

 read();
 
}