console.log("This is my java Script");

const toggleSidebar = () =>{

    if($(".sidebar").is(":visible")){
		
		// true
		// band karna hai

        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");

    }else{
		
		// false		
		// show karna hai

        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");

    }


};