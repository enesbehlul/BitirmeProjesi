Function getMove()
	pacmanKonumu = getPacmanLocation()
	if getPacmanLocation =! NULL then
		sonPacmanKonumu = pacmanKonumu
		mesajGonder(PACMAN_GORULDU)

	if !PACMAN_GORULDU then
		for mesaj in mesajlar
			if mesaj == PACMAN_GORULDU then
				sonPacmanKonumu = mesaj.pacmanKonumu

	if sonPacmanKonumu != NULL then
		return nexMoveTowardsTarget(sonPacmanKonumu)

Function getMove()
	pacmanKonumu = getPacmanLocation()
	if getPacmanLocation =! NULL then
		sonPacmanKonumu = pacmanKonumu
		mesajGonder(PACMAN_GORULDU)

	if !PACMAN_GORULDU then
		for mesaj in mesajlar
			if mesaj == PACMAN_GORULDU then
				sonPacmanKonumu = mesaj.pacmanKonumu

	if sonPacmanKonumu != NULL then
		return nexMoveTowardsTarget(sonPacmanKonumu)



Function getMove()
	konumum = getPacmanLocation()
	for(Ghost hayalet : Ghosts)
		if hayalet.edibleTime == 0
			hayaletKonumu = hayalet.getGhostCurrentNodeIndex()
			if hayaletKonumu != NULL
				return game.getNextMoveAwayFromTarget(konumum, hayaletKonumu)

		if hayalet.edibleTime > 0
			hayaletKonumu = hayalet.getGhostCurrentNodeIndex()
			if hayaletKonumu != NULL
				return game.getNextMoveTowardsTarget(konumum, hayaletKonumu)

		
		
