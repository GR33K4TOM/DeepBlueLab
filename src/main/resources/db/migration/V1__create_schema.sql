CREATE TABLE rescue_centers (
	id BIGSERIAL PRIMARY KEY,
	code VARCHAR(30) NOT NULL UNIQUE,
	name VARCHAR(150) NOT NULL,
	city VARCHAR(100) NOT NULL
);

CREATE TABLE rescue_cases (
	id BIGSERIAL PRIMARY KEY,
	case_code VARCHAR(40) NOT NULL UNIQUE,
	rescue_date DATE NOT NULL,
	rescue_location VARCHAR(200) NOT NULL,
	status VARCHAR(30) NOT NULL,
	rescue_center_id BIGINT NOT NULL,
	CONSTRAINT fk_rescue_case_center
		FOREIGN KEY (rescue_center_id) REFERENCES rescue_centers (id),
	CONSTRAINT chk_rescue_case_status
		CHECK (status IN (
			'ADMITTED',
			'UNDER_EVALUATION',
			'IN_REHABILITATION',
			'READY_FOR_RELEASE',
			'RELEASED',
			'CLOSED'
		))
);

CREATE TABLE animals (
	id BIGSERIAL PRIMARY KEY,
	animal_code VARCHAR(40) NOT NULL UNIQUE,
	common_name VARCHAR(120) NOT NULL,
	scientific_name VARCHAR(160) NOT NULL,
	sex VARCHAR(20) NOT NULL,
	rescue_case_id BIGINT NOT NULL UNIQUE,
	CONSTRAINT fk_animal_rescue_case
		FOREIGN KEY (rescue_case_id) REFERENCES rescue_cases (id)
);

CREATE TABLE medical_records (
	id BIGSERIAL PRIMARY KEY,
	animal_id BIGINT NOT NULL UNIQUE,
	initial_weight NUMERIC(10, 2) NOT NULL,
	initial_condition VARCHAR(100) NOT NULL,
	injuries TEXT,
	observations TEXT,
	CONSTRAINT fk_medical_record_animal
		FOREIGN KEY (animal_id) REFERENCES animals (id)
);

CREATE TABLE specialists (
	id BIGSERIAL PRIMARY KEY,
	professional_code VARCHAR(40) NOT NULL UNIQUE,
	first_name VARCHAR(100) NOT NULL,
	last_name VARCHAR(100) NOT NULL,
	email VARCHAR(200) NOT NULL UNIQUE,
	active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE expertise (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE specialist_expertise (
	specialist_id BIGINT NOT NULL,
	expertise_id BIGINT NOT NULL,
	PRIMARY KEY (specialist_id, expertise_id),
	CONSTRAINT fk_specialist_expertise_specialist
		FOREIGN KEY (specialist_id) REFERENCES specialists (id),
	CONSTRAINT fk_specialist_expertise_expertise
		FOREIGN KEY (expertise_id) REFERENCES expertise (id)
);

CREATE TABLE treatments (
	id BIGSERIAL PRIMARY KEY,
	animal_id BIGINT NOT NULL,
	specialist_id BIGINT NOT NULL,
	performed_at TIMESTAMP NOT NULL,
	type VARCHAR(30) NOT NULL,
	description TEXT,
	CONSTRAINT fk_treatment_animal
		FOREIGN KEY (animal_id) REFERENCES animals (id),
	CONSTRAINT fk_treatment_specialist
		FOREIGN KEY (specialist_id) REFERENCES specialists (id)
);

CREATE INDEX idx_rescue_cases_center ON rescue_cases (rescue_center_id);
CREATE INDEX idx_rescue_cases_status ON rescue_cases (status);
CREATE INDEX idx_rescue_cases_rescue_date ON rescue_cases (rescue_date);
CREATE INDEX idx_treatments_animal ON treatments (animal_id);
CREATE INDEX idx_treatments_specialist ON treatments (specialist_id);
CREATE INDEX idx_treatments_performed_at ON treatments (performed_at);
