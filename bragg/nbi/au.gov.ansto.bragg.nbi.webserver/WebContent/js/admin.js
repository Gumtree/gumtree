function createCode(){
    var postUrl = "jython/user?type=ADDACCOUNT";
    $.post( postUrl, $("form#admin_form").serialize(), function(data, status) {
//        if (status == "success") {
        $("#admin_result").html(data['result']);
        if (data['result'] == 'OK'){
            var html = '<tr>';
            html += '<td>' + $("#login_email").val() + '</td>';
            html += '<td>' + data['code'] + '</td>';
            html += '<td><a href=">' + data['link'] + '">Register</a></td>';
            html += '<td>No</td>';
            $('#table_admin_list > tbody:last').append(html);
        }
    })
    .fail(function(e) {
        alert( "error submitting the script");
    });
}

function checkInputs(){
    var email = $('#login_email').val();
    if (email.indexOf('@') <= 0 || email.indexOf('.') <= 0) {
        $("#admin_result").html("Please type in a valid email address.");
        return false;
    }
    return true;
}

function retrieveAccounts(){
    var getUrl = "jython/user?type=LISTACCOUNTS";
    $.get(getUrl, function(data, status) {
            if (status == "success") {
                if (data.result == "OK") {
                    $.each(data['list'], function(item, value){
                        var html = '<tr>';
                        html += '<td>' + value['email'] + '</td>';
                        html += '<td>' + value['code'] + '</td>';
                        html += '<td><a href="' + value['link'] + '">Register</a></td>';
                        html += '<td>' + (value['status'] ? 'Done' : 'No') + '</td>';
                        $('#table_admin_list > tbody:last').append(html);
                    });
                } else {
                    $("#admin_result").html(data['result']);
                }
            }
        })
        .fail(function(e) {
                alert( "error loading password reset page.");
        });
}

jQuery(document).ready(function(){
    retrieveAccounts();
    
    $('#admin_submit').click(function() {
        if (checkInputs()){
            createCode();
        }
	});
    
    $(document).delegate('#login_email', 'keydown', function(e) {
        var keyCode = e.keyCode || e.which;
        if (keyCode == 13) {
            if (checkInputs()){
                createCode();
            }
        }
    });
});