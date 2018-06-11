package com.microcontrollerbg.usbirtoy.lirc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;

//import com.keanheong.usbirtoy.MainActivity;

//import android.util.Log;

/**
 * As reference http://winlirc.sourceforge.net/technicaldetails.html was used
 */
public class Remote {
	protected String name;	// <remote name> The unique name assigned to the
							// remote control (may not contain whitespace).

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected ArrayList<String> flags;	// flags <flag1><|flag2>. . . etc. Flags are
										// special text strings which describe various
										// properties of the remote, and determine the
										// meaning of some of the following fields.
										// Multiple flags are allowed if separated by
										// the pipe(|) symbol.
	protected HashMap<String, ArrayList<Long>> num_attr = new HashMap<String, ArrayList<Long>>();
	protected HashMap<String, ArrayList<Long>> raw_codes = new HashMap<String, ArrayList<Long>>();
	protected LinkedHashMap<String, Long> codes = new LinkedHashMap<String, Long>();

	boolean toggle_bit_state = false; // initial state shouldn't matter anyway

	Remote() {
		this.name = "Unknown_" + UUID.randomUUID().toString();
		this.num_attr.put("duty_cycle", new ArrayList<Long>(Arrays.asList(50L))); // add default value
	}

	public ArrayList<String> getButtonsNames() {
		ArrayList<String> btnnames = new ArrayList<String>();

		//well, there can be duplicated names, screw it (if sanity check wasn't used)
		for(String btnname : this.codes.keySet()){
			btnnames.add(btnname);
		}

		for(String btnname : this.raw_codes.keySet()){
			btnnames.add(btnname);
		}

		return btnnames;
	}

	public String getButtonsCode(String strBtnName) {
//		Long lPreDataBits = 0;
		Long lPreData = 0L;
		Long lCode = 0L;
		String strHexPreData = "";
		String strHexCode = "";
		String strRet = "";

		// Iterate Through The Set Of KeyValue Pairs, HashMap
		for(String btnname : this.codes.keySet()){
			// Found The Request Button Name
			if (strBtnName.equals(btnname)){
				// Check If Have PreData Field Or Not
				if (this.num_attr.containsKey("pre_data_bits")) {
//					lPreDataBits = this.num_attr.get("pre_data_bits").get(0);
					lPreData = this.num_attr.get("pre_data").get(0);
					strHexPreData = String.format("%X", lPreData);
				}
				lCode = this.codes.get(btnname);
				strHexCode = String.format("%X", lCode);
				strRet = "0x" + strHexPreData + strHexCode;
			}
		}

		return strRet;
	}

	/**
	 * Checking if `Remote' data makes any sense
	 *
	 * @return true if everything looks OK
	 */
	boolean sanityCheck() {
		for (Entry<String, Long> entry : ConfParser.num_attr_supported. entrySet()) {
			if (this.num_attr.containsKey(entry.getKey())) {
				if (entry.getValue() != this.num_attr.get(entry.getKey())
						.size())
					return false;
			}
		}

		for (String key : this.codes.keySet()) {
			if (this.raw_codes.containsKey(key))
				return false;
		}

		return true;
	}

	/**
	 * Render button code to the raw equivalent
	 *
	 * @param code to be converted
	 * @return raw equivalent of 'code'
	 */
	protected ArrayList<Long> codeToRaw(Long code, Long bits) {
		ArrayList<Long> raw = new ArrayList<Long>();
		long lbits = bits;
		int ibits = (int) lbits;
//		MainActivity.log("code="+code);
//		MainActivity.log("lbits="+lbits+" ibits="+ibits);

		if (this.flags.contains("REVERSE")) {
			for (int i = 0; i < ibits; i++) {
				if (((1 << i) & code) == 0) {
					raw.addAll(this.num_attr.get("zero"));
				} else {
					raw.addAll(this.num_attr.get("one"));
				}
			}
		}
		else{
			for (int i = ibits-1; i >= 0; i--) {
//				MainActivity.log("((1 << i) & code)="+((1 << i) & code));
				if (((1 << i) & code) == 0) {
					raw.addAll(this.num_attr.get("zero"));
//					MainActivity.log("i="+i+" 0");
				} else {
					raw.addAll(this.num_attr.get("one"));
//					MainActivity.log("i="+i+" 1");
				}
			}
		}

		return raw;
	}

	private ArrayList<Long> playRaw(ArrayList<Long> raw_code) {
		ArrayList<Long> raw_pulse_space = new ArrayList<Long>();

		if (this.num_attr.containsKey("header")) {
			raw_pulse_space.addAll(this.num_attr.get("header"));
		}

		if (this.num_attr.containsKey("plead")) {
			raw_pulse_space.add(this.num_attr.get("plead").get(0));
//			raw_pulse_space.add(0);// TODO check it! pulse without space after?
		}

		if (this.num_attr.containsKey("pre_data") && this.num_attr.containsKey("pre_data_bits")) {
			raw_pulse_space.addAll(this.codeToRaw(this.num_attr.get("pre_data").get(0),
									this.num_attr.get("pre_data_bits").get(0)));
		}

		if (this.num_attr.containsKey("pre")) {
			raw_pulse_space.addAll(this.num_attr.get("pre"));
		}

		raw_pulse_space.addAll(raw_code);

		if (this.num_attr.containsKey("post")) {
			raw_pulse_space.addAll(this.num_attr.get("post"));
		}

		if (this.num_attr.containsKey("post_data") && this.num_attr.containsKey("post_data_bits")) {
			raw_pulse_space.addAll(this.codeToRaw(this.num_attr.get("post_data").get(0),
									this.num_attr.get("post_data_bits").get(0)));
		}

		if (this.num_attr.containsKey("ptrail")) {
			raw_pulse_space.add(this.num_attr.get("ptrail").get(0));
//			raw_pulse_space.add(0);// TODO check it! pulse without space after?
		}

		// not sure about this placement
		if (this.num_attr.containsKey("repeat_gap")) {
//			raw_pulse_space.add(0);
			raw_pulse_space.add(this.num_attr.get("repeat_gap").get(0));
		}

		// not sure about this placement
		// TODO handle CONST_LENGTH flag
		if (this.num_attr.containsKey("gap")) {
//			raw_pulse_space.add(0);
//			raw_pulse_space.add(this.num_attr.get("gap").get(0));	// ToDo, Disable As Currently Not Support Repeat
		}

		if (this.num_attr.containsKey("foot")) {
			raw_pulse_space.addAll(this.num_attr.get("foot"));
		}

		return raw_pulse_space;
	}

	public ArrayList<Long> playButton(String btn_name) {
		ArrayList<Long> raw;
		if (this.raw_codes.containsKey(btn_name)){
			raw = this.raw_codes.get(btn_name);
		}
		else{
			raw = this.codeToRaw(this.codes.get(btn_name), this.num_attr.get("bits").get(0));
		}

		return this.playRaw(raw);
	}

}
