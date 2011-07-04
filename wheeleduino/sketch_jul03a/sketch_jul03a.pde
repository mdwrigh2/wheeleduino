#include <CRServoLib.h>

//Bot at full speed
Bot bot(15, 14, 750);
int commandByte = 0;
int duration = 20; //miliseconds

//function pointer for motion of bot
void (Bot::*motion)(int) = NULL;

//sensors
int lwhisker = 8;
int rwhisker = 9;
int buzzer = 6;

void setup() {
  Serial.begin(115200);
  pinMode(lwhisker, INPUT);
  pinMode(rwhisker, INPUT);
  pinMode(buzzer, OUTPUT);
  
  while(1){
    Serial.print('r');
    if(Serial.available() > 0){
      break;
    }
  }
  
}

void loop() { 
  if(!digitalRead(lwhisker) || !digitalRead(rwhisker)){
    tone(buzzer, 262, 250);
  }
  
  if(Serial.available() > 0){
    switch(Serial.read()){
      case 'f':
        motion = &Bot::forward;
        break;
      case 'b':
        motion = &Bot::backward;
        break;
      case 'r':
        motion = &Bot::forward_r;
        break;
      case 'l':
        motion = &Bot::forward_l;
        break;
      case 'q':
        motion = &Bot::backward_r;
        break;
      case 'k':
        motion = &Bot::backward_l;
        break;
      case 'c':
        motion = &Bot::spin_cw;
        break;
      case 'w':
        motion = &Bot::spin_ccw;
        break;
      case 's':
      default:
        motion = NULL;
    }
  }
  (bot.*motion)(duration);
}
