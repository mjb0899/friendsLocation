
var baseURL="friends";
var map;
var marker;
var markerArray=[];
function initMap() {
	var uluru = {lat: -25.363, lng: 131.044};
	map = new google.maps.Map(document.getElementById('map'), {
		zoom: 4,
		center: uluru
	}); 
	
	marker = new google.maps.Marker({
		position: uluru,
		map: map,
		draggable: true
	});

	google.maps.event.addListener(marker, 'dragend', function (evt) {
		$('#latitude').val(evt.latLng.lat().toFixed(3));
		$('#longitude').val(evt.latLng.lng().toFixed(3));
	});
}

$("#idclick").click(getUser);


function getUser(){
	var id=$("#idinput").val().trim();
	if(id==""){
		alert("Please Insert id");
	}
	else{
			
		var url=baseURL+"/user/"+id;
		$.ajax(url, {
			success: function(data) {
				
				if(data==null){
					alert("Sorry! User Not Found");
				}else{
					console.log(data)
					currentLocation(data.location.longitude,data.location.latitude);
					getRequest(id);
					for(var x=0; x < markerArray.length ; x++){
						markerArray[x].setMap(null);
					}
					markerArray.length=0;
					getfriends(id);
					$('#idinput').attr('readonly', true);
					$('#idclick').hide();
					$('#idout').show();	

				}
				
				

			},
			error: function(e) {
				alert("Sorry! User Not Found.");
				console.log(e)
			}
		});


	}
}

$('#checkin').on('click', function() {
	var lat = $('#latitude').val().trim();
	var lon = $('#longitude').val().trim();
	var userID = $('#idinput').val().trim();
	
	
	var url = baseURL + '/user/';

	if(lat.length == 0 || lon.length == 0) {
		alert('Please input coordiates!');
	} else {
		$.ajax({
			type: 'PUT',
			url: url,
			data: {lon: lon, lat : lat, user: userID},
			success: function(data) {
				$(".check_status").show();
				$(".check_status").fadeOut(2000);
				
				console.log(data)
			},
			error: function(data) {
				
					alert("Please Login to check in!");
						
			}
		});
	}
});


function currentLocation(lon,lat){

	$("#longitude").val(lon);
	$("#latitude").val(lat);

	var myLatlng = new google.maps.LatLng(lat,lon);
	console.log(lat,lon)
	marker.setPosition(myLatlng)
	map.panTo(myLatlng)
}

$('#sendSub').on('click', function() {
	var url = baseURL + '/subs';
	var subscriberID = $('#idinput').val().trim();
	var subscribeTo = $('#subInput').val().trim();

	if(subscriberID.length == 0 && subscribeTo.length == 0) {
		alert('Make sure you type a user you would like to follow and you\'re signed in!');
	} else {
		$.ajax({
			type: 'POST',
			url: url,
			data: {subscriberID: subscriberID, subscribeTo : subscribeTo},
			success: function(data) {
				console.log(data);
				$('#subInput').val('');
				$(".req_status").show();
				$(".req_status").fadeOut(3000);
			},
			error: function(e) {
				console.log(e)
				alert('No User Found');

			}
		});
	}
});

function getRequest(id){
	var url=baseURL+"/subs/"+id;

	if(id==0){
		alert("Please login");
	}
	else{
		$.ajax(url, {
			success: function(data) 
			{
				$('#requestdiv').show();
				console.log(data)
				$.each(data, function(i, item) {
					var date= new Date(item.timeStamp);
					$("#requestdiv").append('<div class="req_border">'+'<input type="checkbox" id="'+ item.subscriberId +'">'+" "+item.subscriberId+"("+ date +'<br>' +'</div>')
				});
			},
			error: function(e) {
				console.log(e)
			}
		});


	}	
}


$('.replyRequest').on('click', function() {
	var userID = $('#idinput').val().trim();
	console.log(userID);
	var url = baseURL + '/subs/reply';
	var IDs = [];
	var reply = $(this).data('reply');
	$("#requestdiv").find("input:checked").each(function(){ IDs.push(this.id); });

	if(IDs.length == 0 || userID.length == 0) {
		alert('Please select who you would like to aprove or denny');
	} else {
		$.ajax({
			type: 'POST',
			url: url,
			data: {user: userID, requests : IDs.toString(), reply: reply},
			success: function(data) {
				console.log(data)	
			$('#requestdiv').empty();
				$(".req_status").show();
				$(".req_status").fadeOut(3000);
				
				
			},
			error: function(e) {
				console.log(e)
				alert('Error in reply!');

			}
		});
	}
})
//new
function getfriends(id){
	var url=baseURL+"/subs/myfriends";

	if(id==0){
		alert("Please login");
	}
	else{
		$("#subscription_div").html('');
		$.ajax({
			type: 'POST',
			url: url,
			data: {id: id},
			success: function(data) {
				console.log(data)
				$.each(data, function(i, item) {
					var date= new Date(item.lastUpdated);
					$("#subscription_div").append('<div class="friends" data-lon="'+item.location.longitude +'" data-lat="'+item.location.latitude +'" ><strong>'+item.id+'</strong>'+" ("+date+'</div>')
					var uluru = {lat: item.location.latitude, lng:item.location.longitude};
					markerArray[i]= new google.maps.Marker({
						label: item.id.charAt(0),
						icon: 'http://maps.google.com/mapfiles/ms/icons/yellow.png',
						position: uluru,
						map: map,
					});
				});


			},
			error: function(e) {
				console.log(e)
			}
		});
	}	
}

$(document).on('click','.friends',function(){
	
	var myLatlng = new google.maps.LatLng($(this).data("lat"),$(this).data("lon"));
	
	map.panTo(myLatlng);
	map.setZoom(8);
})

$('#ref_request').on('click', function() {
	var userID = $('#idinput').val().trim();
	console.log(userID);
	getRequest(userID);
	
})


$('#idout').on('click', function() {
	
	location.reload(true)
	
})

$('#refresh_btn').on('click', function() {
	var userID = $('#idinput').val().trim();
	console.log(userID);
	getfriends(userID)
		
})

