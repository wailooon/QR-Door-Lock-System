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

firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        var welcomeLable = firebase.database().ref("Users Details/" + user.uid);
        var welcomeLableHtml = "";

        welcomeLable.on("value", function (name) {
            welcomeLableHtml += "<h4 style='padding-top: 10px; margin-left: 15px; font-style: italic; color: rgb(240, 159, 18);'>";
            welcomeLableHtml += "Welcome back, " + name.val().username + " (";
            welcomeLableHtml += name.val().email + ")";
            welcomeLableHtml += "</h4> <br>";
            $("#welcomeLabel").html(welcomeLableHtml);
        });
    }

})

firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        var deviceList = firebase.database().ref("Users Details/" + user.uid + "/Devices/").orderByChild("deviceName");
        var deviceHtml = "";

        deviceList.on("value", function (device) {
            deviceHtml += "<option selected>Choose devices...</option> <br>";
            device.forEach(function (singleDevice) {
                deviceHtml += "<option value=' " + singleDevice.val().deviceName + " '>";
                deviceHtml += singleDevice.val().deviceName;
                deviceHtml += "</option>";
                deviceHtml += "<br>"
            });
            $("#device-List").html(deviceHtml);
        });
    }

})

$(document).ready(function () {
    $('#chooseDevice').click(function (event) {

        var chooseDevice = $('#device option:selected').text();

        firebase.auth().onAuthStateChanged(function (user) {
            if (user) {
                var deviceHistoryList = firebase.database().ref("Users Details/" + user.uid + "/Attempt History/" + chooseDevice);
                var deviceHistoryHtml = "";

                deviceHistoryList.on("value", function (history) {
                    history.forEach(function (singleHistory) {
                        var dateAndTime = singleHistory.val().dateAndTime;
                        var description = singleHistory.val().description;
                        var deviceName = singleHistory.val().deviceName;
                        var doorId = singleHistory.val().doorId;
                        var lock_status = singleHistory.val().lock_Status;


                        deviceHistoryHtml += "<div class='card mb-3'>"
                        deviceHistoryHtml += "<div class = 'card-body'>";
                        deviceHistoryHtml += "<h3 id='deviceHeader' class = 'card-title'>"
                        deviceHistoryHtml += deviceName + " (" + doorId + ")";
                        deviceHistoryHtml += "</h3> <br>";
                        deviceHistoryHtml += "<p class = 'card-text'>" + "<strong>Attempt Location:  </strong>";
                        deviceHistoryHtml += description + "</p>"
                        deviceHistoryHtml += "<p class = 'card-text'>" + "<h4 id='lockStatus'>";
                        deviceHistoryHtml += lock_status + "</h4></p>"
                        deviceHistoryHtml += "<p class = 'card-text'>" + "<strong>Date and Time:  </strong>";
                        deviceHistoryHtml += "<small class = 'text-muted' >";
                        deviceHistoryHtml += dateAndTime + "</small></p>";
                        deviceHistoryHtml += "</div> </div> <br>";

                    });
                    $("#device_history").html(deviceHistoryHtml);
                });


            }

        })

    });
});

firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        var account = firebase.database().ref("Users Details/" + user.uid);
        var accountHtml = "";

        account.on("value", function (name) {
            accountHtml += "<div class='md-form mb-2'>";
            accountHtml += "<i class='fas fa-user prefix grey-text'></i>";
            accountHtml += "<label data-error='wrong' data-success='right' for='settingForm-username'>Username:</label>";
            accountHtml += "<input type='email' value='" + name.val().username + "' id='usernameValue' class='form-control validate'>";
            accountHtml += "</div> <br>";

            accountHtml += "<div class='md-form mb-2'>";
            accountHtml += "<i class='fas fa-envelope prefix grey-text'></i>";
            accountHtml += "<label data-error='wrong' data-success='right' for='settingForm-email'>Email Address:</label>";
            accountHtml += "<input type='text' value='" + name.val().email + "' id='emailValue' class='form-control validate' disabled>";
            accountHtml += "</div> <br>";

            accountHtml += "<div class='md-form mb-2'>";
            accountHtml += "<i class='fas fa-key prefix grey-text'></i>";
            accountHtml += "<label data-error='wrong' data-success='right' for='settingForm-password'>Password:</label>";
            accountHtml += "<input type='text' value='" + name.val().password + "' id='passwordValue' class='form-control validate'>";
            accountHtml += "</div> <br>";

            accountHtml += "<div class='md-form mb-2'>";
            accountHtml += "<i class='fas fa-envelope prefix grey-text'></i>";
            accountHtml += "<label data-error='wrong' data-success='right' for='settingForm-phone'>Phone Number:</label>";
            accountHtml += "<input type='phone' value='" + name.val().phone + "' id='phoneValue' class='form-control validate'>";
            accountHtml += "</div> <br>";

            accountHtml += "<div class='md-form mb-2'>";
            accountHtml += "<i class='fas fa-users-cog prefix grey-text'></i>";
            accountHtml += "<label data-error='wrong' data-success='right' for='settingForm-uid'>User UID:</label>";
            accountHtml += "<input type='text' value='" + name.val().uid + "' id='uidValue' class='form-control validate' disabled>";
            accountHtml += "</div> <br>";

            $("#account").html(accountHtml);
        });
    }

})

$(document).ready(function () {
    $('#updateSetting').click(function (event) {
        firebase.auth().onAuthStateChanged(function (user) {
            if (user) {
                var account = firebase.database().ref("Users Details/" + user.uid);
                var oldPassword;
                var email;

                var newUsernameValue = document.getElementById("usernameValue").value;
                var newPhoneValue = document.getElementById("phoneValue").value;
                var newPasswordValue = document.getElementById("passwordValue").value;

                account.on("value", function (data) {
                    oldPassword = data.val().password;
                    email = data.val().email;
                });

                if (newPasswordValue != "" && newPhoneValue != "") {
                    var user = firebase.auth().currentUser;
                    var credential = firebase.auth.EmailAuthProvider.credential(email, oldPassword);

                    user.reauthenticateWithCredential(credential).then(function () {
                        user.updatePassword(newPasswordValue).then(() => {

                            account.update({
                                username: newUsernameValue,
                                phone: newPhoneValue,
                                password: newPasswordValue
                            })
                            $('#modalAccountForm').modal('hide');
                            window.location.reload(true);
                            window.alert("Success updated setting! ");


                        }, (error) => {
                            var errorCode = error.code;
                            var errorMessage = error.message;

                            console.log(errorCode);
                            console.log(errorMessage);
                            window.alert("Message : " + errorMessage);
                        });
                    }).catch(function (error) {
                        var errorCode = error.code;
                        var errorMessage = error.message;

                        console.log(errorCode);
                        console.log(errorMessage);
                        window.alert("Message : " + errorMessage);
                    });


                } else {
                    window.alert("Please fill out all fields. ");
                }

            }
        })


    });
});










$("#btn-logout").click(function () {
    firebase.auth().signOut();
});

$('.dropdown-toggle').dropdown();