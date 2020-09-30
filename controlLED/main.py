import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import auth
import RPi.GPIO as GPIO
import threading

def listener(event):
    if event.data:
        GPIO.setmode(GPIO.BCM)
        GPIO.setwarnings(False)
        GPIO.setup(17,GPIO.OUT)
        GPIO.setup(18,GPIO.OUT)
        print("LED on")
        GPIO.output(17,True)
        GPIO.output(18,False)

    else:
        GPIO.setmode(GPIO.BCM)
        GPIO.setwarnings(False)
        GPIO.setup(17,GPIO.OUT)
        GPIO.setup(18,GPIO.OUT)
        print("LED off")
        GPIO.output(17,False)
        GPIO.output(18, True)


def fetchCurrentUser(email):
    user = auth.get_user_by_email(email);

    ref = db.reference('Users Details/' + user.uid + '/Devices');

    snapshot = ref.order_by_key().limit_to_first(1).get();
    for key, val in snapshot.items():
        keyValue = key;

    ref = db.reference('Users Details/' + user.uid + '/Devices/' + key + '/lock_Status').listen(listener);

if __name__ == '__main__':
    cred = credentials.Certificate("/home/pi/Desktop/controlLED/qrfacelocksystem-firebase-adminsdk-18wbo-5b597a314c.json")

    firebase_admin.initialize_app(cred, {'databaseURL': 'https://qrfacelocksystem.firebaseio.com/'})

    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    GPIO.setup(17, GPIO.OUT)
    GPIO.setup(18, GPIO.OUT)
    GPIO.output(17, False)
    GPIO.output(18, False)

    # inputEmail = input("Enter email address:\n");
    fetchCurrentUser("williamloo0897@gmail.com");
    # ON_OFF_LED = input("Want to ON LED (Y/N)?\n");
    #
    # if ON_OFF_LED == "Y" or ON_OFF_LED == "y":
    #     inputEmail = input("Enter email address:\n");
    #     fetchCurrentUser(inputEmail);
    # elif ON_OFF_LED == "N" or ON_OFF_LED == "n":
    #     GPIO.setmode(GPIO.BCM)
    #     GPIO.setwarnings(False)
    #     GPIO.setup(17, GPIO.OUT)
    #     GPIO.setup(18,GPIO.OUT)
    #     print("LED off")
    #     GPIO.output(17, False)
    #     GPIO.output(18, False)
    # else:
    #     print("Please enter Y/N !");





