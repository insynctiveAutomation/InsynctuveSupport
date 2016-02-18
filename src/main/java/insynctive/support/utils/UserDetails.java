package insynctive.support.utils;

public enum UserDetails {

	EUGENIO_VALEIRAS("Eugenio Valeiras", "evaleiras@insynctive.com", "@eugeniovaleiras"),
	RODRIGO_GONZALEZ("Rodrigo Gonzalez", "rgonzalez@insynctive.com", "@rgonzalez"),
	BOJAN_PETROVSKI("Bojan Petrovski", "bpetrovski@insynctive.com", "@bpetrovski"),
	MARIJA_JANEVA("Marija Janeva", "mjaneva@insynctive.com", "@mjaneva"),
	ATANAS_TODOROVSKI("Atanas Todorovski", "atodorovski@insynctive.com", "@atanast"),
	CHIP_PETTIBONE("Chip Pettibone", "cpettibone@insynctive.com", "@cpettibone"),
	CRISTI_BOTA("Cristi Bota", "cbota@insynctive.com", "@cristibota"),
	MARTIN_DJNOV("Martin Djonov", "mdjonov@insynctive.com", "@djonov"),
	DIEGO_TRAVIESO("Diego Travieso", "dtravieso@insynctive.com", "@dtravieso"),
	ERIC_KISH("Eric Kish", "ekish@insynctive.com", "@erickish"),
	IGNACIO_FERNANDEZ("Ignacio Fernandez", "ignaciof6@gmail.com", "@ignacio"),
	LJUPCO_SULEV("Ljupco Sulev", "lsulev@insynctive.com", "@ljupco.sulev"),
	ROLAND_ULRICH("Roland Ulrich", "roland.andrei@gmail.com", "@roland"),
	SERHII_LYTVYN("Serhii Lytvyn", "slytvyn@insynctive.com", "@serhii"),
	SIMON_VAZ("Simon Vaz", "svaz@insynctive.com", "@simon"),
	TOME_STOJKOVSKI("Tome Stojkovski", "tstojkovski@insynctive.com", "@tstojkovski"),
	VLATKO_NIKOLOVSKI("Vlatko Nikolovski", "vlatko_n3@hotmail.com", "@vlatko_n3"),
	VOJCHE_STOJANOSKI("Vojche Stojanoski", "vstojanoski@insynctive.com", "@vojche");
	
	public String name;
	public String email;
	public String slackMention;
	
	
	private UserDetails(String name, String email, String slackMention) {
		this.name = name;
		this.email = email;
		this.slackMention = slackMention;
	}
	
	public static UserDetails findByName(String name){
		for(UserDetails userDetail : values()){
			if(userDetail.name.equals(name)){
				return userDetail;
			}
		}
		return null;
	}
	
	public static UserDetails findByEmail(String email){
		for(UserDetails userDetail : values()){
			if(userDetail.email.equals(email)){
				return userDetail;
			}
		}
		return null;
	}
	
	public static UserDetails findBySlackMention(String slackMention){
		for(UserDetails userDetail : values()){
			if(userDetail.slackMention.equals(slackMention)){
				return userDetail;
			}
		}
		return null;
	}

	public static UserDetails findNameByContainString(String nameOfUser) {
		for(UserDetails userDetail : values()){
			if(userDetail.name.contains(nameOfUser)){
				return userDetail;
			}
		}
		return null;
	}
}
