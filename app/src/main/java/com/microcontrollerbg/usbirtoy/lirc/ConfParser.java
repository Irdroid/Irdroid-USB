package com.microcontrollerbg.usbirtoy.lirc;

//import com.keanheong.usbirtoy.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * As reference http://winlirc.sourceforge.net/technicaldetails.html was used
 */
public class ConfParser {
	private final Reader input;
	private final BufferedReader input_br;
	private Stack<String> arglist = new Stack<String>();

	private Stack<String> _parsing_state = new Stack<String>();

	// FIXME this could be avoided, as numerical attributes seems to be
	// contained always one per line - if getNext() would indicate new line,
	// then this can be reimplemented without this map
	public static final Map<String, Long> num_attr_supported; // "attr_name",number_of_numerical_args_expected
	static {
		Map<String, Long> initMap = new HashMap<String, Long>();
		initMap.put("bits", 1L);			// <number of data bits> This is the number of data bits in
											// the hexadecimal codes which describe each button (decimal integer).
		initMap.put("eps", 1L);				// eps <relative error tolerance> The relative
											// error tolerance for received signals in percent (decimal integer).
		initMap.put("aeps", 1L);			// aeps <absolute error tolerance> The absolute
											// error tolerance for received signals in microseconds.
		initMap.put("header", 2L);			// header <phead> <shead> The initial pulse and space sent.
		initMap.put("three", 2L);			// three <pthree> <sthree> Only used by RC-MM remotes
		initMap.put("two", 2L);				// two <ptwo> <stwo> Only used by RC-MM remotes
		initMap.put("one", 2L);				// one <pone> <sone> The pulse and space lengths representing a one.
		initMap.put("zero", 2L);			// zero <pzero> <szero> The pulse and space lengths representing a zero.
		initMap.put("ptrail", 1L);			// ptrail <trailing pulse> A trailing pulse, immediately following the post_data.
		initMap.put("plead", 1L);			// plead <leading pulse> A leading pulse, immediately after the header.
		initMap.put("foot", 2L);			// foot <pfoot> <sfoot> A pulse and space, immediately following the trailing pulse.
		initMap.put("repeat", 2L);			// repeat <prepeat> <srepeat> A pulse and space that replaces everything
											// between leading pulse and the trailing pulse, whenever a signal is
											// repeated. The foot is not sent, and the header is not sent unless the
											// REPEAT_HEADER flag is present.
		initMap.put("pre_data_bits", 1L);	// pre_data_bits <number of pre_data_bits> The number of bits in the pre_data code.
		initMap.put("pre_data", 1L);		// pre_data <hexidecimal number> Hexidecimal code indicating the sequence of
											// ones and zeros immediately following the leading pulse.
		initMap.put("post_data_bits", 1L);	// post_data_bits <number of post data bits> The number of bits in the post_data code.
		initMap.put("post_data", 1L);		// post_data <hexidecimal number> Hexidecimal code indicating the sequence
											// of ones and zeros immediately following the post signal.
		initMap.put("pre", 2L);				// pre <ppre> <spre> A pulse and space immediately following the pre_data.
		initMap.put("post", 2L);			// post <ppost> <spost> A pulse and space immediately following the button code.
		initMap.put("gap", 1L);				// gap <gap length> A (typically long) space which follows the trailing pulse.
		initMap.put("repeat_gap", 1L);		// repeat_gap <repeat_gap length> A gap immediately following
											// the trailing pulse, and preceding a repetition of
											// the same code that's due to a the same press of the button.
		initMap.put("min_repeat", 1L);		// min_repeat <minimum number of repetitions> The minimum times
											// a signal is repeated each time a button is
											// pressed. Note that 0 means the signal is send only one time.
		initMap.put("toggle_bit", 1L);		// toggle_bit <bit to toggle> A bit of the pre_data,
											// code, or post_data that
											// is toggled between one and zero each time a button is pressed.
		initMap.put("frequency", 1L);		// <hertz> The carrier frequency of the remote (default is 38000).
		initMap.put("duty_cycle", 1L);		// duty_cycle <on time> The percentage of time during a pulse that infrared
											// light is being sent (default is 50).
		initMap.put("transmitter", 1L);		// transmitter <hexidecimal number> The
											// number specified is the same as the
											// transmitter type value that appears in the registry. (Not supported
											// by LIRC). The default is DTR with a software carrier. Note WinLIRC will
											// never place this field into a configuration file that it writes.
		num_attr_supported = Collections.unmodifiableMap(initMap);
	}

	public ConfParser(Reader reader) {
		this.input = reader;
		this.input_br = new BufferedReader(this.input, 32768);
	}

	private String getNext() {
		final Pattern comment = Pattern.compile("#.*");
		String arg = null;

		while (arglist != null && arglist.size() == 0) {
			String line;
			try {
				line = input_br.readLine();
			} catch (IOException e) {
				line = null;
				e.printStackTrace();
			}
			if (line != null) {
				line = comment.matcher(line).replaceFirst("").trim();
				String[] slist = line.split("\\s+");
				for (String strTmp : slist) {
					arglist.insertElementAt(strTmp.trim(), 0);
				}
			}
			else {
				arglist = null;
				break;
			}
		}

		if (this.arglist != null) {
			arg = arglist.pop();
		}
		else {
			try {
				input.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		return arg;
	}

	private static Long LongFromString(String arg) throws Exception {
		Long lValue = 0L;

		if (arg.startsWith("0x")) {
			lValue = Long.parseLong(arg.substring(2), 16);
		}
		else {
			lValue = Long.parseLong(arg);
		}

		return lValue;
	}

	public Stack<Remote> Parse() throws Exception {
		Stack<Remote> remotes = new Stack<Remote>();
		String arg;

		while ((arg = this.getNext()) != null) {
			// Error Handling If Any
			if (arg == "") {
				continue;
			}

			// "begin" keyword
			if (arg.equals("begin")) {
				arg = getNext();
				if (arg != null) {
					this._parsing_state.add(arg);
				}
				else{
					throw new Exception("LIRC Parsing error! null after \"begin\"");
				}

				if (!this._parsing_state.empty() && this._parsing_state.peek().equals("remote")){
					remotes.add(new Remote());
				}

				continue;
			}

			// "end" keyword
			if (arg.equals("end")) {
				arg = getNext();
				if (arg != this._parsing_state.peek()) {
					this._parsing_state.pop();
				}
				else{
					throw new Exception("LIRC Parsing error! wrong name after \"end\" keyword");
				}
				continue;
			}

			// section specific - "remote"
			if (!this._parsing_state.empty() && this._parsing_state.peek().equals("remote")) {
				if (arg.equals("name")) {
					arg = getNext();
					if (arg != null) {
						remotes.peek().name = arg;
					}
					else {
						throw new Exception("LIRC Parsing error! \"name\" arg expected");
					}
					continue;
				}

				if (arg.equals("flags")) {
					arg = getNext();
					if (arg != null) {
						remotes.peek().flags = new ArrayList<String>(Arrays.asList(arg.split("|")));
					}
					else{
						throw new Exception("LIRC Parsing error! \"flags\" arg expected");
					}
					continue;
				}

				if (ConfParser.num_attr_supported.containsKey(arg)) {
					String num_attr_name = arg;
					ArrayList<Long> num_attr_val = new ArrayList<Long>();

					for (Long i = ConfParser.num_attr_supported .get(num_attr_name); i > 0; i--) {
						arg = getNext();
						if (arg != null) {
							num_attr_val.add(ConfParser.LongFromString(arg));
						}
						else {
							throw new Exception("LIRC Parsing error! \" + num_attr_name + \" arg expected");
						}
					}

					remotes.peek().num_attr.put(num_attr_name, num_attr_val);
					continue;
				}
			}

			// section specific - "codes"
			if (!this._parsing_state.empty() && this._parsing_state.peek().equals("codes")) {
				String code_name = arg;
				Long code_val;

				arg = getNext();
				if (arg != null) {
					code_val = ConfParser.LongFromString(arg);
				}
				else {
					throw new Exception("LIRC Parsing error! \"code\" arg expected");
				}

				remotes.peek().codes.put(code_name, code_val);
				continue;
			}

		}
		;

		return remotes;
	}

}
