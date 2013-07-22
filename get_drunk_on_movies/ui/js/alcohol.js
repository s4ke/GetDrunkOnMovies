function promille(numberDrinks, drink, sex, weight)
{
	var densityEthanol = 0.79;

	// alcohol per drink in gram
	var drinkSchnaps = 0.02*0.40*densityEthanol*1000.0;
	var drinkBeer = 0.5*0.05*densityEthanol*1000.0;
	var drinkBeerSlug = 0.025*0.05*densityEthanol*1000.0;

	// sex factors
	var factorMale = 0.7;
	var factorFemale = 0.6;
	var factorBaby = 0.8;
	
	var correctionFactor = 0.8;

	// determine factors
	var d; var s;
	switch (drink) {
		case 'schnaps':
			d = drinkSchnaps; break;
		case 'fullbeer':
			d = drinkBeer; break;
		case 'slugbeer':
		default:
			d = drinkBeerSlug; break;
	}
	switch (sex) {
		case 'baby':
			s = factorBaby; break;
		case 'female':
			s = factorFemale; break;
		case 'male':
		default:
			s = factorMale; break;
	}

	// http://de.wikipedia.org/wiki/Blutalkoholkonzentration#Widmark-Formel
	var promille = numberDrinks * d / weight / s;
	promille = promille * correctionFactor;
	promille = promille - 1.5*0.15; // Abbau waehrend Film
	if (promille<0) { promille=0;}
	return promille.toFixed(1);
}
