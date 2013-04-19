package au.gov.ansto.bragg.kookaburra.experiment.model;

/**
 * User model holds a single experiment user details.
 *
 */
public class User extends AbstractModelObject {

	// User name
	private String name;
	
	// User email
	private String email;
	
	// User facility phone number
	private String phone;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		firePropertyChange("name", oldValue, name);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		String oldValue = this.email;
		this.email = email;
		firePropertyChange("email", oldValue, email);
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		String oldValue = this.phone;
		this.phone = phone;
		firePropertyChange("phone", oldValue, phone);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("User [email=");
		builder.append(email);
		builder.append(", name=");
		builder.append(name);
		builder.append(", phone=");
		builder.append(phone);
		builder.append("]");
		return builder.toString();
	}
	
}
