--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


SET search_path = public, pg_catalog;

--
-- Name: event_sequence; Type: SEQUENCE; Schema: public; Owner: pete
--

CREATE SEQUENCE event_sequence
    START WITH 100000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.event_sequence OWNER TO pete;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: event_log; Type: TABLE; Schema: public; Owner: pete; Tablespace: 
--

CREATE TABLE event_log (
    pk integer DEFAULT nextval('event_sequence'::regclass) NOT NULL,
    "timestamp" timestamp without time zone,
    voyage character varying,
    "group" character varying,
    name character varying,
    description character varying,
    "user" character varying,
    latitude double precision,
    longitude double precision,
    altitude double precision,
    heading double precision,
    the_geom geometry
);


ALTER TABLE public.event_log OWNER TO pete;

--
-- Name: parameters; Type: TABLE; Schema: public; Owner: pete; Tablespace: 
--

CREATE TABLE parameters (
    code character(20) NOT NULL,
    description character(256),
    display_code character(10) DEFAULT 'Y'::bpchar,
    netcdf_std_name character varying(255),
    units character varying(40),
    netcdf_long_name character varying(255),
    minimum_valid_value numeric(16,4),
    maximum_valid_value numeric(16,4),
    process character(10) DEFAULT 'N'::bpchar
);


ALTER TABLE public.parameters OWNER TO pete;

--
-- Name: raw_instrument_data; Type: TABLE; Schema: public; Owner: pete; Tablespace: 
--

CREATE TABLE raw_instrument_data (
    voyage character(20) NOT NULL,
    source_file character(20) NOT NULL,
    instrument character(20) NOT NULL,
    data_timestamp timestamp with time zone NOT NULL,
    latitude numeric(16,4) NOT NULL,
    longitude numeric(16,4) NOT NULL,
    depth numeric(16,4) DEFAULT 0 NOT NULL,
    parameter_code character(20) NOT NULL,
    parameter_value numeric(16,4) NOT NULL,
    quality_code character(20) DEFAULT 'N/A'::bpchar NOT NULL
);


ALTER TABLE public.raw_instrument_data OWNER TO pete;

SET default_with_oids = true;

--
-- Name: track; Type: TABLE; Schema: public; Owner: pete; Tablespace: 
--

CREATE TABLE track (
    "timestamp" timestamp without time zone,
    voyage character varying,
    latitude double precision,
    longitude double precision,
    altitude double precision,
    heading double precision,
    the_geom geometry
);


ALTER TABLE public.track OWNER TO pete;

--
-- Name: track_hour; Type: VIEW; Schema: public; Owner: pete
--

CREATE VIEW track_hour AS
 SELECT track."timestamp",
    track.voyage,
    track.latitude,
    track.longitude,
    track.altitude,
    track.heading,
    track.the_geom
   FROM track
  WHERE (((date_part('epoch'::text, track."timestamp"))::integer % 3600) = 0);


ALTER TABLE public.track_hour OWNER TO pete;

--
-- Name: track_line; Type: VIEW; Schema: public; Owner: pete
--

CREATE VIEW track_line AS
 SELECT track.voyage,
    st_makeline(track.the_geom) AS new_geom
   FROM track
  WHERE (((((date_part('epoch'::text, track."timestamp"))::integer % 60) = 0) AND (track."timestamp" < (now() - '01:00:00'::interval))) OR (track."timestamp" >= (now() - '01:00:00'::interval)))
  GROUP BY track.voyage;


ALTER TABLE public.track_line OWNER TO pete;

--
-- Name: track_line_last_hour; Type: VIEW; Schema: public; Owner: pete
--

CREATE VIEW track_line_last_hour AS
 SELECT track.voyage,
    st_makeline(track.the_geom) AS new_geom
   FROM track
  WHERE (track."timestamp" > (now() - '01:00:00'::interval))
  GROUP BY track.voyage;


ALTER TABLE public.track_line_last_hour OWNER TO pete;

--
-- Name: track_line_min; Type: VIEW; Schema: public; Owner: pete
--

CREATE VIEW track_line_min AS
 SELECT track.voyage,
    st_makeline(track.the_geom) AS new_geom
   FROM track
  WHERE ((((date_part('epoch'::text, track."timestamp"))::integer % 60) = 0) AND (track."timestamp" < (now() - '01:00:00'::interval)))
  GROUP BY track.voyage;


ALTER TABLE public.track_line_min OWNER TO pete;

--
-- Name: waypoint_sequence; Type: SEQUENCE; Schema: public; Owner: pete
--

CREATE SEQUENCE waypoint_sequence
    START WITH 100000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.waypoint_sequence OWNER TO pete;

SET default_with_oids = false;

--
-- Name: waypoint; Type: TABLE; Schema: public; Owner: pete; Tablespace: 
--

CREATE TABLE waypoint (
    pk integer DEFAULT nextval('waypoint_sequence'::regclass) NOT NULL,
    "timestamp" timestamp without time zone,
    voyage character varying,
    "group" character varying,
    name character varying,
    description character varying,
    "user" character varying,
    latitude double precision,
    longitude double precision,
    altitude double precision,
    heading double precision,
    the_geom geometry(Point,4326)
);


ALTER TABLE public.waypoint OWNER TO pete;

--
-- Name: event_log_pkey; Type: CONSTRAINT; Schema: public; Owner: pete; Tablespace: 
--

ALTER TABLE ONLY event_log
    ADD CONSTRAINT event_log_pkey PRIMARY KEY (pk);


--
-- Name: track_unique; Type: CONSTRAINT; Schema: public; Owner: pete; Tablespace: 
--

ALTER TABLE ONLY track
    ADD CONSTRAINT track_unique UNIQUE ("timestamp", voyage);


--
-- Name: waypoint_pkey; Type: CONSTRAINT; Schema: public; Owner: pete; Tablespace: 
--

ALTER TABLE ONLY waypoint
    ADD CONSTRAINT waypoint_pkey PRIMARY KEY (pk);


--
-- Name: raw_instrument_data_parameter_code_index; Type: INDEX; Schema: public; Owner: pete; Tablespace: 
--

CREATE INDEX raw_instrument_data_parameter_code_index ON raw_instrument_data USING btree (parameter_code);


--
-- Name: raw_instrument_data_timestamp_index; Type: INDEX; Schema: public; Owner: pete; Tablespace: 
--

CREATE INDEX raw_instrument_data_timestamp_index ON raw_instrument_data USING btree (data_timestamp);


--
-- Name: waypoint_gist; Type: INDEX; Schema: public; Owner: pete; Tablespace: 
--

CREATE INDEX waypoint_gist ON waypoint USING gist (the_geom);


--
-- Name: waypoint_pk; Type: INDEX; Schema: public; Owner: pete; Tablespace: 
--

CREATE UNIQUE INDEX waypoint_pk ON waypoint USING btree (pk);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

