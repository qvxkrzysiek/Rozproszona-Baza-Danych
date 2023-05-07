- Dokumentację poszczególnych funkcji można zobaczyć poprzez uruchomienie Dokumentacja.bat lub index.html w folderze JavaDoc
- Cały projekt działa na protokole TCP
- Można użyć plików .bat do szybkiego uruchomienia węzłów

Organizacja sieci:
1.) Jeśli tworzymy nowy węzęł i chcemy połącyć go z już istniejącym musimy się z nim połączyć i zakomunikować swoją obecność.
Wtedy dopiero możemy mówić o połączeniach węzłów, gdyż bez tego komunikacja byłaby jednostronna.
W tym celu wprowadziłem funkcję new-connection, która jest wysyłana formie String.
2.) Węzły nie różnią się komunikacją od ewentualnego zwykłego klienta, ponieważ łącząc się przez protokół TCP same są klientami.
Aby jednak wiedzieć kto jest kim wprowadziłęm funkcję identify, która jest wysyłana jako pierwsza w formie String. 
Dzięki temu wiemy, że mamy do czynienia z węzłem i możemy poprawnie przeprowadzać pewne redundantne operacje.
3.) Parę funkcji w kodzie jest przeciążonych, aby ułatwić wykonywanie redundantnych operacji.
4.) Unikam niechcianej rekurencji poprzez wysyłanie w formie String napisu identify oraz listy węzłów, które odpowiedzały na to zapytanie.

Kompilowanie:
1.) Przesyłam plik DatabaseNode w formie surowej (.java), a także plik binarny (.class).
2.) Jeśli podstawowe wywołanie bazy danych nie powiodło się ze względu na kompilację należy:
- Otworzyć konsolę w windowsie
- Wejść do pliku używając komendy cd
- Wprowadzić: javac DatabaseNode.java
- Po tych krokach można ponownie wywołać program
3.) Przykładowe wywołanie: java DatabaseNode -tcpport 80 -record 1:25

Informacje na konsoli:
[INFO] -> Informację o aktualnych operacjach węzła
[INFO CORE] -> Linijki otrzymanych informacji od innego węzła
[WARNING] -> Ostrzeżenia o prawdopodobnie błędnym działaniu sieci
[ERROR] -> Krytyczny błąd skutkujący przerwaniem pracy programu

Przykładowe działanie programu przy wywołaniu:
-Uruchomienie
 	main(argumenty wywołania) --> 
		zapisanie i przekazanie dalej 
	DatabaseNode(informacje do tworzenia węzła) -->
	 	zapisuje dane o węźle
		tryToConnect() --> 
			informuje węzły o swoim powstaniu
				sender() -->
					wysyła do węzłów linijkę: new-connection localhost:<PORT>
		showNode -->
			wyświetla informacje o węźle
		listenSocket(<PORT>) -->
			rozpoczyna nasłuchiwanie na podanym porcie
