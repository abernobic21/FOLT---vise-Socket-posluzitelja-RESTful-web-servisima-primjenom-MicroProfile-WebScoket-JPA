FROM ubuntu:latest

COPY abernobic21_vjezba_08_dz_3_partner/target/maven-jlink/default/ /usr/app/

WORKDIR /usr/app/podaci

EXPOSE 8010
EXPOSE 8011


CMD ["sh", "-c", "sleep 4 && /usr/app/bin/java -m abernobic21_vjezba_08_dz_3_partner/edu.unizg.foi.nwtis.abernobic21.vjezba_08_dz_3.PosluziteljPartner NWTiS_04_partner_1.txt PARTNER"]
