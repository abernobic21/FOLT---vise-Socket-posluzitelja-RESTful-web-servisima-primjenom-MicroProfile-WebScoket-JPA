# Sustav za dostavu hrane i pića - FOLT
Projekt FOLT je distribuirani informacijski sustav za simulaciju poslovanja franšize za dostavu hrane i pića. Sustav je razvijan kroz tri faze, počevši od mrežnih utičnica, preko RESTful servisa i ORM-a, do kontejnerizacije i web sučelja.

## Arhitektura sustava
Sustav je organiziran u više modula koji komuniciraju različitim protokolima:

### Poslužitelji (Socket & Multithreading):

Tvrtka: Centralni poslužitelj koji upravlja konfiguracijom, partnerima i jelovnicima.

Partner: Poslužitelji franšizera koji primaju narudžbe.

Komunikacija se odvija putem TCP mrežnih utičnica uz korištenje virtualnih dretvi (Virtual Threads) za visoku konkurentnost.

### RESTful Web Servisi (JPA & REST):

Implementirani su REST API-ji za upravljanje podacima (partneri, jelovnici, narudžbe).

JPA (Jakarta Persistence): Podaci se trajno spremaju u relacijsku bazu podataka (H2/PostgreSQL). Entiteti mapiraju tablice za partnere, cjenike i narudžbe.

Podržani formati: application/json.

### Web Klijent (Jakarta Faces):

Korisničko web sučelje realizirano pomoću Jakarta Faces (JSF) tehnologije.

Omogućuje pregled partnera, jelovnika i kreiranje narudžbi putem preglednika.

### Kontejnerizacija (Docker):

Sustav je u potpunosti kontejneriziran.

Docker Compose: Orkestracija svih servisa (baza, REST API, socket poslužitelji).

JLink: Kreirane su optimizirane (custom) Java slike za module tvrtka i partner radi smanjenja veličine kontejnera.

## Tehnologije
Jezik: Java 21+

Build alat: Maven (multi-module)

Baza podataka: H2 / PostgreSQL (JPA entiteti)

Web servisi: Jakarta REST (JAX-RS)

Web sučelje: Jakarta Faces (JSF)

Infrastruktura: Docker, Docker Compose

Ostalo: JSON, Gson, Virtual Threads

