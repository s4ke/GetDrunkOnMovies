$(document).ready(function() {
	$('#test').click(function() {
		hideElement($('#test'));
	});
	
	apeInit();
	apeWalk();
});

function apeInit() {
	var ape = $('#ape');
	ape.css({
		'top' : '30',
		'left' : '-164'
	});
}

function apeWalk() {
	var ape = $('#ape');
	ape.removeClass('ds-ui-ape-flip');
	ape.delay(800).animate({'left' : $(window).width()}, 20000, apeWalkBack);
}

function apeWalkBack() {
	var ape = $('#ape');
	ape.addClass('ds-ui-ape-flip');
	ape.delay(800).animate({'left' : -64}, 20000, apeWalk);
}

function hideElement(ele) {
	$(ele).css('position', 'relative');
	$(ele).animate({
			'opacity' : '0.0',
			'top' : '30'
		}, 
		400, 
		function() {
			$(ele).css({
				'position'	: '', 
				'top' 		: '0',
				'display' 	: 'none'
			});
		}
	);
}

function showAll() {
		$.getJSON("http://localhost:8080/?allMovies", {}, 
			function(data) {
			alert(data);
			var display = $('#display');
			display.html('');
			for(var i = 0; i < data.length; ++i) {
				var movieTitleContainer = $('<div class="movieTitle">' + (i+1) + '. ' + data[i] + '</div>');
				movieTitleContainer.data('name', data[i]);
				display.append(movieTitleContainer);
			}
			addEvents();
	});
}
		
function showOne(movie) {
	$.getJSON("http://localhost:8080/", {
			'movie' : movie
		}, function(data) {
			var display = $('#display');
			display.html('');
			for(var i = 0; i < data.length; ++i) {
				var instance = data[i];
				addMovieToDiv(display, instance);
			}
			addEvents();
		});
}
		
function addMovieToDiv(div, movie) {
	var drink = movie['drink'];
	var name = movie['name'];
	if(drink !== null && name !== null) {
		var movieTitleContainer = $('<div class="movieTitle">' + name + '</div>');
		movieTitleContainer.data('name', name);
		div.append(movieTitleContainer);
		var drinkContainer = $('<div>');
		for(var occasion in drink) {
			var drinkDiv = $('<div>');
			drinkDiv.attr('class', 'drink');
			drinkDiv.append(occasion + ' : ' + drink[occasion]);
			drinkContainer.append(drinkDiv);
		}
		div.append(drinkContainer);
	}
}
		
function addEvents() {
	$('.movieTitle').click(function(event) {
		showOne($(event.target).data('name'));
	});
}