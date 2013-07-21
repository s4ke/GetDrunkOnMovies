var densityEthanol = 0.79;

// alcohol per drink in gram
var drinkSchnaps = 0.02*0.40*densityEthanol*1000.0;
var drinkBeer = 0.5*0.05*densityEthanol*1000.0;
var drinkBeerSlug = 0.025*0.05*densityEthanol*1000.0;

// correction factors
var factorMale = 0.7;
var factorFemale = 0.6;
var factorBaby = 0.8;

function promille(numberDrinks, drink, factor, weight)
{
	// http://de.wikipedia.org/wiki/Blutalkoholkonzentration#Widmark-Formel
	var promille = numberDrinks * drink / weight / factor;
	promille = promille * 0.85;
	promille = promille - 1.5*0.15; // Abbau waehrend Film
	if (promille<0) { promille=0;}
	return promille.toFixed(1);
}
