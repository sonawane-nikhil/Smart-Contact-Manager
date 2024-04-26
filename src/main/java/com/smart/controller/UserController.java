package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
//	method for adding common data to response
	
	@ModelAttribute
	public void addCommonData(Model m,Principal principal)
	{
		
		String userName = principal.getName();
		System.out.println("USERNAME "+userName);
		
		//get the user using username(Email)
			
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("USER "+user);
		
		m.addAttribute("user",user);
	}
	
//	DashBoard Home
	@RequestMapping("/index")
	public String dashboard(Model m)
	{
		m.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
//	open add form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m) 
	{
		m.addAttribute("title","Add Contact");
		m.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
//	processing add contact form
	
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session) 
	{
		
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			
			if(file.isEmpty()) 
			{
				// if the file is empty then try our message
				
				System.out.println("File is Empty..");
				
				contact.setImage("contact.png");
			}else 
			{
				// file the file to folder and update the name to create to contact
				contact.setImage(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is Uploaded");
			}
			
			contact.setUser(user);
			
			user.getContacts().add(contact);
			
			this.userRepository.save(user);
			
			System.out.println("DATA"+contact);
			
			System.out.println("Added to data base");
			
			// message success....
			
			session.setAttribute("message", new Message("Your Contect is added !! Add more..", "success"));
		} 
		catch (Exception e)
		{
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			
			//message Error
			session.setAttribute("message", new Message("Some went wrong !! Try Again.. ", "danger"));
			
		}
		
		return "normal/add_contact_form";
	}

	// show contact handler
	// per page = 5[n]
	// current page = 0 [page]s
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page,Model m,Principal principal) 
	{
		m.addAttribute("title","Show User Contacts");
		
		// contact ki list ko bhejni hai
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
			Pageable pageable = PageRequest.of(page, 3);
	
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	// showing perticular contact details
	
	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable ("cId") Integer cId,Model m,Principal principal) 
	{
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptinal = this.contactRepository.findById(cId);
		Contact contact = contactOptinal.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) 
		{
			m.addAttribute("contact",contact);
			m.addAttribute("title",contact.getName());
		} 
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable ("cId") Integer cId, Model m,HttpSession session,Principal principal) 
	{
//		Optional<Contact> contactOptinal = this.contactRepository.findById(cId);
//		Contact contact = contactOptinal.get();
		
		  Contact contact = this.contactRepository.findById(cId).get();
		
		// check...
		System.out.println("contact "+contact.getcId());
		
//		contact.setUser(null);
//		this.contactRepository.delete(contact);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		
		
		System.out.println("Deleted");
		session.setAttribute("message", new Message("Contact Deleted successfully...","success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	//Open update form handler
	
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cid,Model m) 
	{
		
		m.addAttribute("title","Update Contact");
		
		  Contact contact = this.contactRepository.findById(cid).get();
		  
		  m.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	// Update contact handler
	
//	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	@PostMapping("/process-update")
	
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			Model m,HttpSession session,Principal principal) 
	{
		
		try {
			// old contact details
			
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			
			// image select ki hai
			if(!file.isEmpty()) 
			{
				// file work
				// rewrite
				
				// delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();
				
				
				// update new photo
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				
			}else 
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is updated...", "success"));
			
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		System.out.println("contact name"+contact.getName());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	// Profile handler
	
	@GetMapping("/profile")
	public String yourProfile(Model m) 
	{
		m.addAttribute("title","Profile Page");
		
		return "normal/profile";
	}
}

















