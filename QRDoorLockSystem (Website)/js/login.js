var firebaseConfig = {
    apiKey: "AIzaSyBJO_Nb4oZ-DSuDCcUkJo5sCegBQX3A9Hk",
    authDomain: "qrfacelocksystem.firebaseapp.com",
    databaseURL: "https://qrfacelocksystem.firebaseio.com",
    projectId: "qrfacelocksystem",
    storageBucket: "qrfacelocksystem.appspot.com",
    messagingSenderId: "795392676502",
    appId: "1:795392676502:web:899bfc02d734d6415028b7",
    measurementId: "G-0Z44SNYZWP"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
firebase.analytics();

firebase.auth.Auth.Persistence.LOCAL;

$("#btn-login").click(function () {
    var email = $("#email").val();
    var password = $("#password").val();


    if (email != "" && password != "") {
        var result = firebase.auth();


        result.signInWithEmailAndPassword(email, password).then(() => {
            window.alert("Successful logged!")
        }).catch(function (error) {
            var errorCode = error.code;
            var errorMessage = error.message;

            console.log(errorCode);
            console.log(errorMessage);
            window.alert("Message : " + errorMessage);

        });

    } else {
        window.alert("Form is incomplete, Please fill out all fields. ");
    }
});