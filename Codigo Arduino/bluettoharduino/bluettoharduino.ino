#include <SoftwareSerial.h>
#include <EEPROM.h>
#include <WISOL.h>
#include <Tsensors.h>
#include <math.h>
#include <SimpleTimer.h>

int relay = 7;
SoftwareSerial blue(2, 3); // 2 RX, 3 TX.
Isigfox *Isigfox = new WISOL();
Tsensors *tSensors = new Tsensors();

char NOMBRE[21] = "HC-06";
char BPS = '4';
char PASS[5] = "1234";
char modo = 'r';
char usuario = ' ';
float tiempo_sis = 60UL;
float i = 0;
float tiempo_total;


float movimiento = 0;
#define AI_BATTERY A0
int analogValue = 0;
float voltage = 0.00;

typedef union {
  uint16_t number;
  uint8_t bytes[2];
} UINT16_t;

typedef union {
  int16_t number;
  uint8_t bytes[2];
} INT16_t;



void setup() {
  pinMode(relay,OUTPUT);
  int flagInit;

  flagInit = -1;
  while (flagInit == -1) {
    Serial.println(""); // Make a clean restart
    delay(1000);
    flagInit = Isigfox->initSigfox();
    Isigfox->testComms();
    GetDeviceID();
    //Isigfox->setPublicKey(); // set public key for usage with SNEK
  }
  tSensors->initSensors();
  Serial.println(""); // Make a clean start
  delay(1000);

  blue.begin(9600);
  pinMode(13, OUTPUT);
  digitalWrite(13, HIGH);
  delay(4000);

  digitalWrite(13, LOW);
  blue.print("AT");
  delay(1000);

  blue.print("AT+NAME=");
  blue.print(NOMBRE);
  delay(1000);

  blue.print("AT+BAUD");
  blue.print(BPS);
  delay(1000);

  blue.print("AT+PIN");
  blue.print(PASS);
  delay(1000);

}

void loop() {
  analogValue =  analogRead(AI_BATTERY);
  voltage = map(analogValue, 0, 1023, 0, 100);
  if (blue.available()) {
    usuario = blue.read();
    Serial.println(usuario);
  }
  if (usuario == '+' || usuario == 'D' || usuario == 'I' || usuario == 'S' || usuario == 'C' || usuario == 'S' || usuario == 'U' || usuario == 'E' || usuario == 'O' || usuario == 'K' ||  usuario == ':' || usuario == '\n' || usuario == ' ') {
    Serial.print("EPROM: ");
    Serial.println(EEPROM.get(0, usuario));
    usuario = EEPROM.get(0, usuario);
  }
  if (usuario == 'e') {
    modo = 'e';
    Serial.println("MODO ESTATICO");
  } else if (usuario == 'r') {
    modo = 'r';
    Serial.println("MODO REAL");
  }
  if (modo == 'e') {
    Serial.println("MODO ESTATICO");
    delay(1000);
    if (usuario == '0' || usuario == '1' || usuario == '2' || usuario == '3' || usuario == '4' || usuario == '5' || usuario == '6' || usuario == '7' || usuario == '8' || usuario == '9') {
      int tiempo = String(usuario).toInt();
      tiempo_total = tiempo_sis * tiempo;
      EEPROM.put(0, usuario);
      if (i < tiempo_total) {
        obtener_aceleracion();
      } else {
        modo = 'r';
        i = 0;
      }
      i++;
      Serial.println(i);
      delay(1000);
    }
    //obtener_aceleracion();
  } else if (modo == 'r') {
    Serial.println("MODO REAL");
    enviarSigfox(voltage, 1.0);
    activar_ibutton();
    desactivar_ibutton();
    //delay(600000);//tiempo de espera 10 minutos
    delay(60000);
  }
  delay(1000);
}


void obtener_aceleracion() {
  INT16_t x_g, y_g, z_g;
  acceleration_xyz *xyz_g;
  xyz_g = (acceleration_xyz *)malloc(sizeof(acceleration_xyz));
  tSensors->getAccXYZ(xyz_g);
  x_g.number = (int16_t) (xyz_g->x_g * 250);
  y_g.number = (int16_t) (xyz_g->y_g * 250);
  z_g.number = (int16_t) (xyz_g->z_g * 250);

  float x = (float)x_g.number / 250;
  float y = (float)y_g.number / 250;
    Serial.print("Acc X: "); Serial.println(x);
  Serial.print("Acc Y: "); Serial.println(y);
  if (abs(x) > 1.5 or abs(y) > 0.1) {
    if (movimiento == 0 or movimiento == 500UL) {
      Serial.println("ENVIANDO DATOS...");
      delay(5000);
      enviarSigfox(voltage, 1.0);
      activar_ibutton();
      desactivar_ibutton();
      //Serial.print("Acc X: "); Serial.println(x);
      //Serial.print("Acc Y: "); Serial.println(y);
      Serial.println("DATOS ENVIADO SATISFACTORIAMENTE...");
    } else if (movimiento > 500UL) {
      movimiento = 0;
    }
    movimiento++;
  } else {
    movimiento = 0;
  }

  Serial.print("\0");
  free(xyz_g);
  delay(1000);
}


void GetDeviceID() {
  recvMsg *RecvMsg;
  const char msg[] = "AT$I=10";

  RecvMsg = (recvMsg *)malloc(sizeof(recvMsg));
  Isigfox->sendMessage(msg, 7, RecvMsg);

  Serial.print("Device ID: ");
  for (int i = 0; i < RecvMsg->len; i++) {
    Serial.print(RecvMsg->inData[i]);
  }
  Serial.println("");
  free(RecvMsg);
}


void enviarSigfox(float voltajeMedido, float activMovimiento) {
  byte *float_volt = (byte *)&voltajeMedido;
  byte *float_movimiento = (byte *)&activMovimiento;
  const uint8_t payloadSize = 9;
  uint8_t buf_str[payloadSize];
  buf_str[0] = float_volt[0];
  buf_str[1] = float_volt[1];
  buf_str[2] = float_volt[2];
  buf_str[3] = float_volt[3];
  buf_str[4] = float_movimiento[0];
  buf_str[5] = float_movimiento[1];
  buf_str[6] = float_movimiento[2];
  buf_str[7] = float_movimiento[3];

  uint8_t *sendData = buf_str;
  Send_Pload(buf_str, payloadSize);
}

void Send_Pload(uint8_t *sendData, const uint8_t len) {
  recvMsg *RecvMsg;
  RecvMsg = (recvMsg *)malloc(sizeof(recvMsg));
  Isigfox->sendPayload(sendData, len, 0, RecvMsg);
  for (int i = 0; i < RecvMsg->len; i++) {
    Serial.print(RecvMsg->inData[i]);
  }
  Serial.println("");
  free(RecvMsg);
}

void activar_ibutton(){
  digitalWrite(relay,HIGH);
  Serial.println("ACTIVADO");
  delay(5000);
}

void desactivar_ibutton(){
  digitalWrite(relay,LOW);
  Serial.println("DESACTIVADO");
  delay(1000);
}
