function get_cookie ( cookie_name )
{
	// http://www.thesitewizard.com/javascripts/cookies.shtml
	var cookie_string = document.cookie ;
	//console.log("got cookie: " + cookie_string);
	if (cookie_string.length != 0) {
		var cookie_value = cookie_string.match ( '(^|;)[\s]*' + cookie_name + '=([^;]*)' );
		if (cookie_value === null) return null;
		return decodeURIComponent ( cookie_value[2] ) ;
	}
	return null;
}

function writeCookie()
{
	var cookie = "drunk=" + drinkFactor + "," + sexFactor + "," + weightFactor + "; max-age=" + 60*60*24*365 + "; path=/; domain=getdrunkonmovies.com;";
	//console.log("writing cookie: "+cookie);
	document.cookie = cookie;
}

function restoreFromCookie()
{
	var drunk=get_cookie("drunk");
	if (drunk !== null) {
		var values=drunk.split(",");
		drinkFactor=values[0];
		sexFactor=values[1];
		weightFactor=values[2];
		console.log("restored Values: " +drinkFactor +"|"+ sexFactor +"|"+ weightFactor);
	}
	else {
		drinkFactor='slugbeer';
		sexFactor='male';
		weightFactor='70';
		writeCookie();
	}
}
