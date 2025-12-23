package someoneok.kic.models.kuudra;

import java.util.HashMap;
import java.util.Map;

public final class ElleDialogues {
    public static final Map<String, String> ROADMAN = unmodifiable(new HashMap<String, String>() {{

        // ── Intro / Fishing setup ───────────────────────────────────────────────────
        put("[NPC] Elle: Talk with me to begin!",
                "§e[NPC] §cElle§f: Oi fam, come chat with me if you wanna kick this ting off, innit.");

        put("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!",
                "§e[NPC] §cElle§f: Aight mandem, I’m off to bait up big Kuudra, safe?");

        put("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!",
                "§e[NPC] §cElle§f: Roll to the main platform, I’ll pull up when I get a nibble, yeah?");

        // ── Hooking Kuudra ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Nope, that's just a Magmafish!",
                "§e[NPC] §cElle§f: Nahh, man just snagged a Magmafish, dead ting.");

        put("[NPC] Elle: Wait... I think I've hooked onto something!",
                "§e[NPC] §cElle§f: Hold up… think I’ve hooked somethin’ serious still.");

        put("[NPC] Elle: Whatever it is, it sure is big! This might be it!",
                "§e[NPC] §cElle§f: Whatever that is, it’s hench fam—this might be the one!");

        put("[NPC] Elle: Uh oh.",
                "§e[NPC] §cElle§f: Ahhh allow it…");

        put("[NPC] Elle: Not again!",
                "§e[NPC] §cElle§f: Not this again man, long day!");

        // ── Supplies phase ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Phew... that was close!",
                "§e[NPC] §cElle§f: Rah… that was bare close, fam!");

        put("[NPC] Elle: ARGH! All of the supplies fell into the lava! You need to retrieve them quickly!",
                "§e[NPC] §cElle§f: ARGH! Man’s supplies dropped in the lava—§cpattern that quick§f!");

        put("[NPC] Elle: Use your Lava Fishing Rods to pull them closer to the platform and collect them!",
                "§e[NPC] §cElle§f: Use your §cLava Rods§f to §cpull the bits in§f and scoop ’em up, yeah!");

        put("[NPC] Elle: I'll help you clear out Kuudra's minions, go fetch my supplies!",
                "§e[NPC] §cElle§f: I’ll clear these ops—§cyou grab my gear§f, say less!");

        put("[NPC] Elle: OMG! Great work collecting my supplies!",
                "§e[NPC] §cElle§f: Wagwan—nice one collectin’ my bits, fam!");

        put("[NPC] Elle: Now... please just cover me for a few minutes, I'll put together our special weapon!",
                "§e[NPC] §cElle§f: Safe—cover me for a minute, I’ll slap together our special weapon ting.");

        put("[NPC] Elle: It's time to build the Ballista again! Cover me!",
                "§e[NPC] §cElle§f: Time to slap the Ballista back together fam — §cback me§f!");

        // ── Horde / Immune phase ───────────────────────────────────────────────────
        put("[NPC] Elle: What the hell is that noise?! I think we've made him mad!",
                "§e[NPC] §cElle§f: Yo what’s that racket?! Man’s vexxed now, you know!");

        put("[NPC] Elle: Looks like the horde is coming! Cover me!",
                "§e[NPC] §cElle§f: Horde inbound—§cback me§f!");

        put("[NPC] Elle: Damaging Kuudra directly is futile. His thick skin is impenetrable with conventional weapons!",
                "§e[NPC] §cElle§f: Direct hits are pointless, fam—§chis skin’s too brolic§f for normal straps.");

        put("[NPC] Elle: I'm sure that'll buff out...",
                "§e[NPC] §cElle§f: It’ll polish out… probably.");

        put("[NPC] Elle: Argh, that didn't sound healthy.",
                "§e[NPC] §cElle§f: Eesh, that sounded mad unhealthy still.");

        put("[NPC] Elle: BRUH",
                "§e[NPC] §cElle§f: BRUH.");

        put("[NPC] Elle: Guys!! You need to focus the tentacles!",
                "§e[NPC] §cElle§f: Oi mandem—focus the tentacles, pattern it!");

        put("[NPC] Elle: That's a lot of damage!",
                "§e[NPC] §cElle§f: Man’s doin’ hella damage, say no more!");

        // ── Ballista build / fuel ───────────────────────────────────────────────────
        put("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!",
                "§e[NPC] §cElle§f: Rah! Ballista’s live—should tank Kuudra’s swings now, trust.");

        put("[NPC] Elle: We need to find the fuel for the Ballista, it must have fallen in the lava with the rest of my supplies!",
                "§e[NPC] §cElle§f: Need fuel for the Ballista—must’ve dropped in the lava with the rest of my stash.");

        put("[NPC] Elle: I'm sure that'll buff out... I hope..",
                "§e[NPC] §cElle§f: It'll polish out... I hope, fam.");

        // ── Re-emerge / Fire ballista ───────────────────────────────────────────────
        put("[NPC] Elle: Huh, what is that noise?",
                "§e[NPC] §cElle§f: Oi, wagwan with that noise bruv?");

        put("[NPC] Elle: Looks like Kuudra has re-emerged! It's time to shoot him with the Ballista!",
                "§e[NPC] §cElle§f: Man like Kuudra’s back—§clet the Ballista rip§f!");

        put("[NPC] Elle: Whow.. WHOW! What's happening!!",
                "§e[NPC] §cElle§f: Ayyo—what’s this madness poppin’ off?!");

        // ── Eaten / Belly (muffled) ────────────────────────────────────────────────
        put("Elle has been eaten by Kuudra!",
                "§cElle just got gobbled by Kuudra fam—peak!");

        put("[NPC] Elle: *muffled* Help! I'm still alive in here! It stinks!",
                "§e[NPC] §cElle§f: §8*muffled*§f Yo help—still alive in here, smells §cpeak§f!");

        put("[NPC] Elle: *muffled*I think I'm in Kuudra's Belly! I was standing too close!",
                "§e[NPC] §cElle§f: §8*muffled*§f Think I’m in §cKuudra’s belly§f—stood too close innit!");

        put("[NPC] Elle: *muffled* See if you can find a way to get in here too! I see something which may help us kill Kuudra!",
                "§e[NPC] §cElle§f: §8*muffled*§f Try §cgettin’ in here too§f—spotted a ting that might help us bun Kuudra!");

        put("[NPC] Elle: *muffled* Hurry!",
                "§e[NPC] §cElle§f: §8*muffled*§f Hurry up, fam!");

        // ── Inside belly gameplay ───────────────────────────────────────────────────
        put("[NPC] Elle: Oh, funny seeing you here.",
                "§e[NPC] §cElle§f: Rah, fancy seein’ you in these ends.");

        put("[NPC] Elle: What? Yes I am still alive, I landed inside Kuudra's belly here.",
                "§e[NPC] §cElle§f: What, shocked? I’m calm—landed in Kuudra’s belly, innit.");

        put("[NPC] Elle: It looks like if we mine these purple pods, we can do some real damage to Kuudra!",
                "§e[NPC] §cElle§f: If we §cmine them purple pods§f, we’ll do §cserious damage§f to Kuudra.");

        put("[NPC] Elle: Oh come on! What is it this time!",
                "§e[NPC] §cElle§f: Allow it man—what now?!");

        put("[NPC] Elle: I'll fight the mobs, you focus on destroying those pods!",
                "§e[NPC] §cElle§f: I’ll handle the mobs—§cyou mash those pods§f!");

        // ── Submerge / Escape / Flush ───────────────────────────────────────────────
        put("[NPC] Elle: The lava level is rising inside Kuudra! He must have submerged under the lava!",
                "§e[NPC] §cElle§f: Lava’s risin’ inside—bro must’ve dipped under the lava!");

        put("[NPC] Elle: We need to get out before we burn in this lava!",
                "§e[NPC] §cElle§f: We gotta §ccut§f before this lava cooks us!");

        put("[NPC] Elle: We can escape back out his throat at the top of his stomach!",
                "§e[NPC] §cElle§f: We can §cescape through his throat§f—top of the belly, move it!");

        put("[NPC] Elle: aaaaaaaaaaaaaaaaaaaaaaaaaaaah",
                "§e[NPC] §cElle§f: AAAAAAAAAA—this is long!");

        put("[NPC] Elle: When Kuudra submerges, we will be flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: When he dips we get §cflushed out§f—need to get back in, but mind it when he submerges!");

        put("[NPC] Elle: Kuudra submerged and I was flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: He submerged and §cflushed me out§f—back inside ASAP, but be careful when he dips!");

        put("[NPC] Elle: Kuudra has these purple pods in his stomach which look vulnerable! If we mine the pods we might be able to damage Kuudra!",
                "§e[NPC] §cElle§f: He’s got §cpurple pods§f in the gut—mine ’em and we can §cclart Kuudra§f!");

        // ── Stun / Ballista window ─────────────────────────────────────────────────
        put("[NPC] Elle: That looks like it hurt! Quickly, while Kuudra is distracted, shoot him with the Ballista!",
                "§e[NPC] §cElle§f: That looked peak—quick, while he’s stunned, §clet the Ballista sing§f!");

        // ── Event callouts (adds/minions) ───────────────────────────────────────────
        put("[NPC] Elle: A group of Wandering Blazes are emerging from Kuudra's mouth!",
                "§e[NPC] §cElle§f: Group of §cWandering Blazes§f comin’ out the gob—pattern ’em!");

        put("[NPC] Elle: Look out, Magma Followers! Don't let them merge!",
                "§e[NPC] §cElle§f: Heads up—§cMagma Followers§f! Don’t let ’em fuse!");

        put("[NPC] Elle: A Dropship is approaching! Take it down before it's too late!",
                "§e[NPC] §cElle§f: §cDropship§f inbound—take it down quick!");

        put("[NPC] Elle: A fleet of Dropships are approaching! Take them down before it's too late!",
                "§e[NPC] §cElle§f: Whole fleet of §cDropships§f rollin’ up—clear the skies!");

        put("[NPC] Elle: Those Energized Chunks really pack a punch when they explode! Try hitting them towards Kuudra!",
                "§e[NPC] §cElle§f: Them §cEnergized Chunks§f slap hard—bat ’em towards Kuudra!");

        // ── Ends / Phases ───────────────────────────────────────────────────────────
        put("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!",
                "§e[NPC] §cElle§f: POW! That’s gotta be it—man’s got nothin’ left!");

        put("[NPC] Elle: I knew you could do it!",
                "§e[NPC] §cElle§f: I knew you had it, say less!");

        put("[NPC] Elle: One last flail before admitting defeat, huh?",
                "§e[NPC] §cElle§f: One last lil’ wiggle before he clocks it’s over, yeah?");

        put("[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!",
                "§e[NPC] §cElle§f: Big ups everyone—hard scrap done. Let’s cut before more madness pulls up.");

        put("[NPC] Elle: What's happening? Why isn't he dying?",
                "§e[NPC] §cElle§f: Wagwan—how’s man not droppin’ still?");

        put("[NPC] Elle: What just happened!? Is this Kuudra's real lair?",
                "§e[NPC] §cElle§f: Oi, what just popped off? Is this his real yard?");

        put("[NPC] Elle: ....",
                "§e[NPC] §cElle§f: ....");

        put("[NPC] Elle: I'm not going down there... that's all you.",
                "§e[NPC] §cElle§f: I ain’t steppin’ down there fam—that one’s on you.");
    }});

    public static final Map<String, String> PIRATE = unmodifiable(new HashMap<String, String>() {{

        // ── Intro / Fishing setup ───────────────────────────────────────────────────
        put("[NPC] Elle: Talk with me to begin!",
                "§e[NPC] §cElle§f: Arrr matey, step up an’ parley if ye be seekin’ adventure!");

        put("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!",
                "§e[NPC] §cElle§f: Aye crew, I’ll cast me line an’ hook that sea-devil Kuudra!");

        put("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!",
                "§e[NPC] §cElle§f: Make way to the main deck—I'll join ye when the beast takes the bait!");

        // ── Hooking Kuudra ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Nope, that's just a Magmafish!",
                "§e[NPC] §cElle§f: Belay that—jus’ a blasted Magmafish, ye scallywags.");

        put("[NPC] Elle: Wait... I think I've hooked onto something!",
                "§e[NPC] §cElle§f: Avast… me hook’s snagged somethin’ fierce!");

        put("[NPC] Elle: Whatever it is, it sure is big! This might be it!",
                "§e[NPC] §cElle§f: By the brine, she’s a biggun! This could be the monster itself!");

        put("[NPC] Elle: Uh oh.",
                "§e[NPC] §cElle§f: …Well shiver me timbers.");

        put("[NPC] Elle: Not again!",
                "§e[NPC] §cElle§f: Blast it all—again?!");

        // ── Supplies phase ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Phew... that was close!",
                "§e[NPC] §cElle§f: Har! That were a near miss, mateys.");

        put("[NPC] Elle: ARGH! All of the supplies fell into the lava! You need to retrieve them quickly!",
                "§e[NPC] §cElle§f: ARRR! Me stores be overboard in the lava—§cfetch ’em smartly§f!");

        put("[NPC] Elle: Use your Lava Fishing Rods to pull them closer to the platform and collect them!",
                "§e[NPC] §cElle§f: Man yer §cLava Rods§f—§chaul the crates to the deck§f an’ stow ’em!");

        put("[NPC] Elle: I'll help you clear out Kuudra's minions, go fetch my supplies!",
                "§e[NPC] §cElle§f: I’ll cut down Kuudra’s bilge-rats—§cye fetch me cargo§f!");

        put("[NPC] Elle: OMG! Great work collecting my supplies!",
                "§e[NPC] §cElle§f: Yo-ho! Fine work recoverin’ me booty!");

        put("[NPC] Elle: Now... please just cover me for a few minutes, I'll put together our special weapon!",
                "§e[NPC] §cElle§f: Stand fast an’ §ccover me§f—I'll rig our secret cannon-contraption!");

        put("[NPC] Elle: It's time to build the Ballista again! Cover me!",
                "§e[NPC] §cElle§f: Time to rebuild the Ballista, lads—§cform a guard ’round me§f!");

        // ── Horde / Immune phase ───────────────────────────────────────────────────
        put("[NPC] Elle: What the hell is that noise?! I think we've made him mad!",
                "§e[NPC] §cElle§f: By the depths, what devil’s din is that?! We’ve riled the beast!");

        put("[NPC] Elle: Looks like the horde is coming! Cover me!",
                "§e[NPC] §cElle§f: The scurvy horde approaches—§cstand to an’ cover me§f!");

        put("[NPC] Elle: Damaging Kuudra directly is futile. His thick skin is impenetrable with conventional weapons!",
                "§e[NPC] §cElle§f: ’Tis folly to strike him straight on—§chis hide be thicker than a galleon’s hull§f!");

        put("[NPC] Elle: I'm sure that'll buff out...",
                "§e[NPC] §cElle§f: Aye… a wee scrape—she’ll buff out… mayhap.");

        put("[NPC] Elle: Argh, that didn't sound healthy.",
                "§e[NPC] §cElle§f: Arrr, that clatter sounded none too seaworthy.");

        put("[NPC] Elle: BRUH",
                "§e[NPC] §cElle§f: …Yarr.");

        put("[NPC] Elle: Guys!! You need to focus the tentacles!",
                "§e[NPC] §cElle§f: Hands! Put yer steel to them tentacles—focus fire!");

        put("[NPC] Elle: That's a lot of damage!",
                "§e[NPC] §cElle§f: Ho ho! That’s a broadside worth singin’ about!");

        // ── Ballista build / fuel ───────────────────────────────────────────────────
        put("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!",
                "§e[NPC] §cElle§f: Ballista’s primed an’ true—she’ll weather Kuudra’s blows now!");

        put("[NPC] Elle: We need to find the fuel for the Ballista, it must have fallen in the lava with the rest of my supplies!",
                "§e[NPC] §cElle§f: We be needin’ fuel—must’ve sunk to the lava with t’other stores. Find it!");

        put("[NPC] Elle: I'm sure that'll buff out... I hope..",
                "§e[NPC] §cElle§f: It’ll buff out… I hope, by the tides.");

        // ── Re-emerge / Fire ballista ───────────────────────────────────────────────
        put("[NPC] Elle: Huh, what is that noise?",
                "§e[NPC] §cElle§f: Hark—what be that rumblin’, ye dogs?");

        put("[NPC] Elle: Looks like Kuudra has re-emerged! It's time to shoot him with the Ballista!",
                "§e[NPC] §cElle§f: Kuudra surfaces! §cBring the Ballista to bear an’ fire§f!");

        put("[NPC] Elle: Whow.. WHOW! What's happening!!",
                "§e[NPC] §cElle§f: Avast! What sorcery be this?!");

        // ── Eaten / Belly (muffled) ────────────────────────────────────────────────
        put("Elle has been eaten by Kuudra!",
                "§cElle’s been swallowed whole by Kuudra—cursed seas!");

        put("[NPC] Elle: *muffled* Help! I'm still alive in here! It stinks!",
                "§e[NPC] §cElle§f: §8*muffled*§f Help! I yet draw breath—an’ it reeks like bilge!");

        put("[NPC] Elle: *muffled*I think I'm in Kuudra's Belly! I was standing too close!",
                "§e[NPC] §cElle§f: §8*muffled*§f I’m in §cKuudra’s belly§f—stood too near the maw!");

        put("[NPC] Elle: *muffled* See if you can find a way to get in here too! I see something which may help us kill Kuudra!",
                "§e[NPC] §cElle§f: §8*muffled*§f Find a way aboard this gut—spied a thing might §cscuttle Kuudra§f!");

        put("[NPC] Elle: *muffled* Hurry!",
                "§e[NPC] §cElle§f: §8*muffled*§f Make haste, ye lubbers!");

        // ── Inside belly gameplay ───────────────────────────────────────────────────
        put("[NPC] Elle: Oh, funny seeing you here.",
                "§e[NPC] §cElle§f: Well I’ll be—fancy meetin’ ye in this cursed crawl.");

        put("[NPC] Elle: What? Yes I am still alive, I landed inside Kuudra's belly here.",
                "§e[NPC] §cElle§f: Aye, still kickin’—washed up right in Kuudra’s gut, I did.");

        put("[NPC] Elle: It looks like if we mine these purple pods, we can do some real damage to Kuudra!",
                "§e[NPC] §cElle§f: Crack them §cpurple pods§f—’twill deal §cproper hurt§f to the beast!");

        put("[NPC] Elle: Oh come on! What is it this time!",
                "§e[NPC] §cElle§f: By Neptune’s beard—what now?!");

        put("[NPC] Elle: I'll fight the mobs, you focus on destroying those pods!",
                "§e[NPC] §cElle§f: I’ll fend the riffraff—§cye smash the pods§f!");

        // ── Submerge / Escape / Flush ───────────────────────────────────────────────
        put("[NPC] Elle: The lava level is rising inside Kuudra! He must have submerged under the lava!",
                "§e[NPC] §cElle§f: The lava be risin’—the fiend’s dived beneath the fire!");

        put("[NPC] Elle: We need to get out before we burn in this lava!",
                "§e[NPC] §cElle§f: We must away ere we’re cooked alive!");

        put("[NPC] Elle: We can escape back out his throat at the top of his stomach!",
                "§e[NPC] §cElle§f: There’s our out—§cup his throat, top o’ the gullet§f!");

        put("[NPC] Elle: aaaaaaaaaaaaaaaaaaaaaaaaaaaah",
                "§e[NPC] §cElle§f: AAAAAAAAA—by the briny deep!");

        put("[NPC] Elle: When Kuudra submerges, we will be flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: When he dives we’re §cflushed from his belly§f—back inside smartly, but mind the submerge!");

        put("[NPC] Elle: Kuudra submerged and I was flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: He dove an’ §cspat me out§f—back aboard the belly, but keep yer weather eye open!");

        put("[NPC] Elle: Kuudra has these purple pods in his stomach which look vulnerable! If we mine the pods we might be able to damage Kuudra!",
                "§e[NPC] §cElle§f: In his gut lie §cpurple pods§f—mine ’em and we’ll §crend Kuudra§f!");

        // ── Stun / Ballista window ─────────────────────────────────────────────────
        put("[NPC] Elle: That looks like it hurt! Quickly, while Kuudra is distracted, shoot him with the Ballista!",
                "§e[NPC] §cElle§f: That strike bit deep! Quick—while he’s distracted, §clet the Ballista thunder§f!");

        // ── Event callouts (adds/minions) ───────────────────────────────────────────
        put("[NPC] Elle: A group of Wandering Blazes are emerging from Kuudra's mouth!",
                "§e[NPC] §cElle§f: A clutch o’ §cWanderin’ Blazes§f from his maw—send ’em to Davy Jones!");

        put("[NPC] Elle: Look out, Magma Followers! Don't let them merge!",
                "§e[NPC] §cElle§f: Beware them §cMagma Followers§f—don’t let the curs merge!");

        put("[NPC] Elle: A Dropship is approaching! Take it down before it's too late!",
                "§e[NPC] §cElle§f: §cDropship§f inbound—down her masts afore it’s too late!");

        put("[NPC] Elle: A fleet of Dropships are approaching! Take them down before it's too late!",
                "§e[NPC] §cElle§f: A whole fleet o’ §cDropships§f—blast ’em from the sky!");

        put("[NPC] Elle: Those Energized Chunks really pack a punch when they explode! Try hitting them towards Kuudra!",
                "§e[NPC] §cElle§f: Them §cEnergized Chunks§f blow like powder kegs—bat ’em into Kuudra!");

        // ── Ends / Phases ───────────────────────────────────────────────────────────
        put("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!",
                "§e[NPC] §cElle§f: POW! That must do it—the knave’s spent, I wager!");

        put("[NPC] Elle: I knew you could do it!",
                "§e[NPC] §cElle§f: I knew ye had the salt for it!");

        put("[NPC] Elle: One last flail before admitting defeat, huh?",
                "§e[NPC] §cElle§f: One last flop afore the white flag, eh?");

        put("[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!",
                "§e[NPC] §cElle§f: Fine work, crew—battle’s won. Weigh anchor afore more trouble finds us!");

        put("[NPC] Elle: What's happening? Why isn't he dying?",
                "§e[NPC] §cElle§f: What deviltry is this—why won’t the cur sink?");

        put("[NPC] Elle: What just happened!? Is this Kuudra's real lair?",
                "§e[NPC] §cElle§f: By thunder—what just transpired? Be this Kuudra’s true lair?");

        put("[NPC] Elle: ....",
                "§e[NPC] §cElle§f: ……."); // dramatic pirate pause

        put("[NPC] Elle: I'm not going down there... that's all you.",
                "§e[NPC] §cElle§f: I’ll not be goin’ down yonder—this voyage be yours, matey.");
    }});

    public static final Map<String, String> ZOOMER = unmodifiable(new HashMap<String, String>() {{

        // ── Intro / Fishing setup ───────────────────────────────────────────────────
        put("[NPC] Elle: Talk with me to begin!",
                "§e[NPC] §cElle§f: yo pull up n chat if you wanna start this W run");

        put("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!",
                "§e[NPC] §cElle§f: aight squad, i’m gonna reel in big kuudra, bet");

        put("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!",
                "§e[NPC] §cElle§f: head to main plat, i’ll tp in when i get a nibble");

        // ── Hooking Kuudra ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Nope, that's just a Magmafish!",
                "§e[NPC] §cElle§f: nvm it’s a magmafish");

        put("[NPC] Elle: Wait... I think I've hooked onto something!",
                "§e[NPC] §cElle§f: wait hold up… got something thicc on the line");

        put("[NPC] Elle: Whatever it is, it sure is big! This might be it!",
                "§e[NPC] §cElle§f: whatever this is, it’s HUGE—this might be the one");

        put("[NPC] Elle: Uh oh.",
                "§e[NPC] §cElle§f: uh oh");

        put("[NPC] Elle: Not again!",
                "§e[NPC] §cElle§f: bruh not again");

        // ── Supplies phase ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Phew... that was close!",
                "§e[NPC] §cElle§f: phew, close call ngl");

        put("[NPC] Elle: ARGH! All of the supplies fell into the lava! You need to retrieve them quickly!",
                "§e[NPC] §cElle§f: AHH supplies went yeet into lava—§cgrab ’em fast§f");

        put("[NPC] Elle: Use your Lava Fishing Rods to pull them closer to the platform and collect them!",
                "§e[NPC] §cElle§f: use §cLava Rods§f to §cdrag loot in§f then scoop, EZ");

        put("[NPC] Elle: I'll help you clear out Kuudra's minions, go fetch my supplies!",
                "§e[NPC] §cElle§f: i’ll clap the adds—§cyou fetch my stuff§f, team diff");

        put("[NPC] Elle: OMG! Great work collecting my supplies!",
                "§e[NPC] §cElle§f: OMG W team, supplies secured");

        put("[NPC] Elle: Now... please just cover me for a few minutes, I'll put together our special weapon!",
                "§e[NPC] §cElle§f: cover me a sec, crafting our giga-weapon");

        put("[NPC] Elle: It's time to build the Ballista again! Cover me!",
                "§e[NPC] §cElle§f: rebuild time—Ballista v2, §ccover me§f");

        // ── Horde / Immune phase ───────────────────────────────────────────────────
        put("[NPC] Elle: What the hell is that noise?! I think we've made him mad!",
                "§e[NPC] §cElle§f: what is that noise?? bro’s malding now");

        put("[NPC] Elle: Looks like the horde is coming! Cover me!",
                "§e[NPC] §cElle§f: incoming mob wave—§ccover me§f, don’t int");

        put("[NPC] Elle: Damaging Kuudra directly is futile. His thick skin is impenetrable with conventional weapons!",
                "§e[NPC] §cElle§f: direct dps is 0 dmg, §chis skin = pay2win armor§f");

        put("[NPC] Elle: I'm sure that'll buff out...",
                "§e[NPC] §cElle§f: pretty sure that’ll buff out… copium");

        put("[NPC] Elle: Argh, that didn't sound healthy.",
                "§e[NPC] §cElle§f: yikes that sound was not OSHA compliant");

        put("[NPC] Elle: BRUH",
                "§e[NPC] §cElle§f: BRUH");

        put("[NPC] Elle: Guys!! You need to focus the tentacles!",
                "§e[NPC] §cElle§f: focus tentacles pls, stop split dmg");

        put("[NPC] Elle: That's a lot of damage!",
                "§e[NPC] §cElle§f: that’s a TON of dmg, W burst");

        // ── Ballista build / fuel ───────────────────────────────────────────────────
        put("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!",
                "§e[NPC] §cElle§f: Ballista online should face-tank kuudra now");

        put("[NPC] Elle: We need to find the fuel for the Ballista, it must have fallen in the lava with the rest of my supplies!",
                "§e[NPC] §cElle§f: need fuel—probably yeeted into lava with the stash, go go");

        put("[NPC] Elle: I'm sure that'll buff out... I hope..",
                "§e[NPC] §cElle§f: it’ll buff out… i hope… pls");

        // ── Re-emerge / Fire ballista ───────────────────────────────────────────────
        put("[NPC] Elle: Huh, what is that noise?",
                "§e[NPC] §cElle§f: huh? new sfx just dropped");

        put("[NPC] Elle: Looks like Kuudra has re-emerged! It's time to shoot him with the Ballista!",
                "§e[NPC] §cElle§f: kuudra respawned—§csend the Ballista, full send§f");

        put("[NPC] Elle: Whow.. WHOW! What's happening!!",
                "§e[NPC] §cElle§f: WHOA WHOA what is happening rn");

        // ── Eaten / Belly (muffled) ────────────────────────────────────────────────
        put("Elle has been eaten by Kuudra!",
                "§cElle just got hard-swallowed by Kuudra… skill issue? (jk)");

        put("[NPC] Elle: *muffled* Help! I'm still alive in here! It stinks!",
                "§e[NPC] §cElle§f: §8*muffled*§f help!! still alive in here and it smells like hard L");

        put("[NPC] Elle: *muffled*I think I'm in Kuudra's Belly! I was standing too close!",
                "§e[NPC] §cElle§f: §8*muffled*§f i’m literally in §cKuudra’s belly§f… stood too close, my bad");

        put("[NPC] Elle: *muffled* See if you can find a way to get in here too! I see something which may help us kill Kuudra!",
                "§e[NPC] §cElle§f: §8*muffled*§f try to §cget in here too§f—i see a win condition, free dub");

        put("[NPC] Elle: *muffled* Hurry!",
                "§e[NPC] §cElle§f: §8*muffled*§f hurry plz");

        // ── Inside belly gameplay ───────────────────────────────────────────────────
        put("[NPC] Elle: Oh, funny seeing you here.",
                "§e[NPC] §cElle§f: lmao fancy seeing you in the gut zone");

        put("[NPC] Elle: What? Yes I am still alive, I landed inside Kuudra's belly here.",
                "§e[NPC] §cElle§f: yeah still alive, soft landed in kuudra’s tummy");

        put("[NPC] Elle: It looks like if we mine these purple pods, we can do some real damage to Kuudra!",
                "§e[NPC] §cElle§f: mine the §cpurple pods§f for big dmg—free value");

        put("[NPC] Elle: Oh come on! What is it this time!",
                "§e[NPC] §cElle§f: bruh what now");

        put("[NPC] Elle: I'll fight the mobs, you focus on destroying those pods!",
                "§e[NPC] §cElle§f: i’ll peel the mobs—§cyou delete the pods§f, division of labor");

        // ── Submerge / Escape / Flush ───────────────────────────────────────────────
        put("[NPC] Elle: The lava level is rising inside Kuudra! He must have submerged under the lava!",
                "§e[NPC] §cElle§f: lava level up—bro went underwater update");

        put("[NPC] Elle: We need to get out before we burn in this lava!",
                "§e[NPC] §cElle§f: we gotta bounce before we get cooked");

        put("[NPC] Elle: We can escape back out his throat at the top of his stomach!",
                "§e[NPC] §cElle§f: exit route = §cup the throat§f, speedrun strat");

        put("[NPC] Elle: aaaaaaaaaaaaaaaaaaaaaaaaaaaah",
                "§e[NPC] §cElle§f: AAAAAAAAA—this dungeon is wild");

        put("[NPC] Elle: When Kuudra submerges, we will be flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: when he dips we get §cflushported§f—get back in but mind the timing");

        put("[NPC] Elle: Kuudra submerged and I was flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: he submerged → §cyeet out of stomach§f—re-entry asap, careful mechanics");

        put("[NPC] Elle: Kuudra has these purple pods in his stomach which look vulnerable! If we mine the pods we might be able to damage Kuudra!",
                "§e[NPC] §cElle§f: belly has §cpurple weak pods§f—mine = big dmg, free DPS check");

        // ── Stun / Ballista window ─────────────────────────────────────────────────
        put("[NPC] Elle: That looks like it hurt! Quickly, while Kuudra is distracted, shoot him with the Ballista!",
                "§e[NPC] §cElle§f: he’s staggered—§cfire Ballista now§f, burst window GO");

        // ── Event callouts (adds/minions) ───────────────────────────────────────────
        put("[NPC] Elle: A group of Wandering Blazes are emerging from Kuudra's mouth!",
                "§e[NPC] §cElle§f: adds spawn—§cWandering Blazes§f from his mouth, kite & delete");

        put("[NPC] Elle: Look out, Magma Followers! Don't let them merge!",
                "§e[NPC] §cElle§f: watch the §cMagma Followers§f—do NOT let them fuse, that’s a wipe");

        put("[NPC] Elle: A Dropship is approaching! Take it down before it's too late!",
                "§e[NPC] §cElle§f: §cDropship§f inbound—anti-air go brrrr");

        put("[NPC] Elle: A fleet of Dropships are approaching! Take them down before it's too late!",
                "§e[NPC] §cElle§f: fleet of §cDropships§f—clear skies or L raid");

        put("[NPC] Elle: Those Energized Chunks really pack a punch when they explode! Try hitting them towards Kuudra!",
                "§e[NPC] §cElle§f: §cEnergized Chunks§f go boom—bat them into kuudra for max meme dmg");

        // ── Ends / Phases ───────────────────────────────────────────────────────────
        put("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!",
                "§e[NPC] §cElle§f: POW! that’s gotta be it—man’s out of gas fr fr");

        put("[NPC] Elle: I knew you could do it!",
                "§e[NPC] §cElle§f: i knew y’all would clutch, W team");

        put("[NPC] Elle: One last flail before admitting defeat, huh?",
                "§e[NPC] §cElle§f: one last flop before GG, huh?");

        put("[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!",
                "§e[NPC] §cElle§f: ggs everyone, sweaty fight—let’s dip before part 2 spawns");

        put("[NPC] Elle: What's happening? Why isn't he dying?",
                "§e[NPC] §cElle§f: hello?? why won’t he die, scuffed boss");

        put("[NPC] Elle: What just happened!? Is this Kuudra's real lair?",
                "§e[NPC] §cElle§f: bro what just happened—this the real lair DLC??");

        put("[NPC] Elle: ....",
                "§e[NPC] §cElle§f: …");

        put("[NPC] Elle: I'm not going down there... that's all you.",
                "§e[NPC] §cElle§f: i’m not going down there lol, that’s on you chief");
    }});

    public static final Map<String, String> EMO = unmodifiable(new HashMap<String, String>() {{

        // ── Intro / Fishing setup ───────────────────────────────────────────────────
        put("[NPC] Elle: Talk with me to begin!",
                "§e[NPC] §cElle§f: Speak to me… if you dare begin this cycle of suffering.");

        put("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!",
                "§e[NPC] §cElle§f: Very well… I’ll summon the monster that lurks beneath these cursed waves.");

        put("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!",
                "§e[NPC] §cElle§f: Go… to the platform. I’ll arrive when darkness pulls something to my line.");

        // ── Hooking Kuudra ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Nope, that's just a Magmafish!",
                "§e[NPC] §cElle§f: Hah… only another worthless Magmafish. Meaningless.");

        put("[NPC] Elle: Wait... I think I've hooked onto something!",
                "§e[NPC] §cElle§f: Hold… the abyss has answered. Something vast claws at my hook…");

        put("[NPC] Elle: Whatever it is, it sure is big! This might be it!",
                "§e[NPC] §cElle§f: Whatever this dread shape is, it is colossal… perhaps our doom has arrived.");

        put("[NPC] Elle: Uh oh.",
                "§e[NPC] §cElle§f: …Despair.");

        put("[NPC] Elle: Not again!",
                "§e[NPC] §cElle§f: Again fate mocks us. Again it drags us down.");

        // ── Supplies phase ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Phew... that was close!",
                "§e[NPC] §cElle§f: That was nearly the end… of what little hope we cling to.");

        put("[NPC] Elle: ARGH! All of the supplies fell into the lava! You need to retrieve them quickly!",
                "§e[NPC] §cElle§f: ARGH! The fire consumes all we treasure—§cfetch them before nothing remains§f!");

        put("[NPC] Elle: Use your Lava Fishing Rods to pull them closer to the platform and collect them!",
                "§e[NPC] §cElle§f: Use thy rods of flame to §cpull back what remains§f from the inferno…");

        put("[NPC] Elle: I'll help you clear out Kuudra's minions, go fetch my supplies!",
                "§e[NPC] §cElle§f: I’ll carve down these wretched fiends—§cyou salvage what ashes of my supplies you can§f.");

        put("[NPC] Elle: OMG! Great work collecting my supplies!",
                "§e[NPC] §cElle§f: Incredible… you actually pulled hope from the flames. Maybe life isn’t entirely hollow.");

        put("[NPC] Elle: Now... please just cover me for a few minutes, I'll put together our special weapon!",
                "§e[NPC] §cElle§f: Guard me awhile… I must forge a weapon in this darkness… forged from despair itself.");

        put("[NPC] Elle: It's time to build the Ballista again! Cover me!",
                "§e[NPC] §cElle§f: Again… the Ballista must rise from ruin. §cProtect me or it all crumbles§f.");

        // ── Horde / Immune phase ───────────────────────────────────────────────────
        put("[NPC] Elle: What the hell is that noise?! I think we've made him mad!",
                "§e[NPC] §cElle§f: What cursed noise rends the silence? Kuudra stirs, and his fury is endless.");

        put("[NPC] Elle: Looks like the horde is coming! Cover me!",
                "§e[NPC] §cElle§f: The horde of shadows draws near—§cstand guard, or be swallowed§f.");

        put("[NPC] Elle: Damaging Kuudra directly is futile. His thick skin is impenetrable with conventional weapons!",
                "§e[NPC] §cElle§f: Our blows bounce from his husk. §cNo mortal weapon can pierce this abyssal shell§f.");

        put("[NPC] Elle: I'm sure that'll buff out...",
                "§e[NPC] §cElle§f: Perhaps it will mend… but scars never truly fade.");

        put("[NPC] Elle: Argh, that didn't sound healthy.",
                "§e[NPC] §cElle§f: That sound… like bones cracking beneath inevitability.");

        put("[NPC] Elle: BRUH",
                "§e[NPC] §cElle§f: …Bruh. Even the void sighs.");

        put("[NPC] Elle: Guys!! You need to focus the tentacles!",
                "§e[NPC] §cElle§f: Focus your rage upon the writhing limbs—end their grasp on our souls!");

        put("[NPC] Elle: That's a lot of damage!",
                "§e[NPC] §cElle§f: Such ruin… such exquisite ruin.");

        // ── Ballista build / fuel ───────────────────────────────────────────────────
        put("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!",
                "§e[NPC] §cElle§f: The Ballista breathes again—strong enough to meet Kuudra’s wrath… perhaps.");

        put("[NPC] Elle: We need to find the fuel for the Ballista, it must have fallen in the lava with the rest of my supplies!",
                "§e[NPC] §cElle§f: The Ballista thirsts for fuel—lost among the flames, like all else we love.");

        put("[NPC] Elle: I'm sure that'll buff out... I hope..",
                "§e[NPC] §cElle§f: It may mend… I hope… though hope is such a fragile lie.");

        // ── Re-emerge / Fire ballista ───────────────────────────────────────────────
        put("[NPC] Elle: Huh, what is that noise?",
                "§e[NPC] §cElle§f: The air quakes… what fresh torment is this?");

        put("[NPC] Elle: Looks like Kuudra has re-emerged! It's time to shoot him with the Ballista!",
                "§e[NPC] §cElle§f: Kuudra returns from the abyss—§cstrike with the Ballista, make him bleed despair§f!");

        put("[NPC] Elle: Whow.. WHOW! What's happening!!",
                "§e[NPC] §cElle§f: The world itself shatters… we are unmoored!!");

        // ── Eaten / Belly (muffled) ────────────────────────────────────────────────
        put("Elle has been eaten by Kuudra!",
                "§cKuudra’s maw has claimed me—swallowed by darkness eternal!");

        put("[NPC] Elle: *muffled* Help! I'm still alive in here! It stinks!",
                "§e[NPC] §cElle§f: §8*muffled*§f Help! I yet breathe in this pit of rot… it reeks of death.");

        put("[NPC] Elle: *muffled*I think I'm in Kuudra's Belly! I was standing too close!",
                "§e[NPC] §cElle§f: §8*muffled*§f I dwell now in §cKuudra’s belly§f—consumed by my own folly.");

        put("[NPC] Elle: *muffled* See if you can find a way to get in here too! I see something which may help us kill Kuudra!",
                "§e[NPC] §cElle§f: §8*muffled*§f Find your way inside—within lies the means to end Kuudra’s reign of sorrow!");

        put("[NPC] Elle: *muffled* Hurry!",
                "§e[NPC] §cElle§f: §8*muffled*§f Hurry… before the void takes me whole!");

        // ── Inside belly gameplay ───────────────────────────────────────────────────
        put("[NPC] Elle: Oh, funny seeing you here.",
                "§e[NPC] §cElle§f: Strange… that we should meet again… in the womb of despair.");

        put("[NPC] Elle: What? Yes I am still alive, I landed inside Kuudra's belly here.",
                "§e[NPC] §cElle§f: Yes, still alive… cast into this cavern of misery that is Kuudra’s gut.");

        put("[NPC] Elle: It looks like if we mine these purple pods, we can do some real damage to Kuudra!",
                "§e[NPC] §cElle§f: Shatter the §cpurple pods§f—each crack is agony for the beast!");

        put("[NPC] Elle: Oh come on! What is it this time!",
                "§e[NPC] §cElle§f: Must sorrow never end? What torment assails us now?!");

        put("[NPC] Elle: I'll fight the mobs, you focus on destroying those pods!",
                "§e[NPC] §cElle§f: I’ll cut down these shades—§cyou crush the pods of weakness§f!");

        // ── Submerge / Escape / Flush ───────────────────────────────────────────────
        put("[NPC] Elle: The lava level is rising inside Kuudra! He must have submerged under the lava!",
                "§e[NPC] §cElle§f: The lava climbs… Kuudra sinks into the abyss once more.");

        put("[NPC] Elle: We need to get out before we burn in this lava!",
                "§e[NPC] §cElle§f: Escape now, lest we be ash like all forgotten dreams.");

        put("[NPC] Elle: We can escape back out his throat at the top of his stomach!",
                "§e[NPC] §cElle§f: Our only way out… up through the throat of this nightmare.");

        put("[NPC] Elle: aaaaaaaaaaaaaaaaaaaaaaaaaaaah",
                "§e[NPC] §cElle§f: AAAAAAAAA—anguish unending!!");

        put("[NPC] Elle: When Kuudra submerges, we will be flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: When he dives, we are §cejected into the void§f—beware, re-enter with care.");

        put("[NPC] Elle: Kuudra submerged and I was flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: He sank, and I was §ccast out into despair§f—return with caution!");

        put("[NPC] Elle: Kuudra has these purple pods in his stomach which look vulnerable! If we mine the pods we might be able to damage Kuudra!",
                "§e[NPC] §cElle§f: Inside lie §cpurple pods§f—break them, and bleed Kuudra’s black heart!");

        // ── Stun / Ballista window ─────────────────────────────────────────────────
        put("[NPC] Elle: That looks like it hurt! Quickly, while Kuudra is distracted, shoot him with the Ballista!",
                "§e[NPC] §cElle§f: That wound staggered him—§cstrike now with the Ballista, bring him misery§f!");

        // ── Event callouts (adds/minions) ───────────────────────────────────────────
        put("[NPC] Elle: A group of Wandering Blazes are emerging from Kuudra's mouth!",
                "§e[NPC] §cElle§f: From his maw spew §cWandering Blazes§f—spawn of fire and sorrow!");

        put("[NPC] Elle: Look out, Magma Followers! Don't let them merge!",
                "§e[NPC] §cElle§f: Beware the §cMagma Followers§f—if they unite, despair reigns eternal!");

        put("[NPC] Elle: A Dropship is approaching! Take it down before it's too late!",
                "§e[NPC] §cElle§f: §cDropship§f descends—strike it down ere it crushes all hope!");

        put("[NPC] Elle: A fleet of Dropships are approaching! Take them down before it's too late!",
                "§e[NPC] §cElle§f: A fleet darkens the skies—bring them low or drown in grief!");

        put("[NPC] Elle: Those Energized Chunks really pack a punch when they explode! Try hitting them towards Kuudra!",
                "§e[NPC] §cElle§f: These §cEnergized Chunks§f burst like grief itself—hurl them at Kuudra!");

        // ── Ends / Phases ───────────────────────────────────────────────────────────
        put("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!",
                "§e[NPC] §cElle§f: POW! Surely his torment is complete—he can suffer no more…");

        put("[NPC] Elle: I knew you could do it!",
                "§e[NPC] §cElle§f: I knew… even in shadow, you’d prevail.");

        put("[NPC] Elle: One last flail before admitting defeat, huh?",
                "§e[NPC] §cElle§f: One last thrash before surrender… classic.");

        put("[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!",
                "§e[NPC] §cElle§f: Well done, my cursed companions. Let us flee ere more shadows follow.");

        put("[NPC] Elle: What's happening? Why isn't he dying?",
                "§e[NPC] §cElle§f: Why does death not claim him? Is he eternal, as sorrow is?");

        put("[NPC] Elle: What just happened!? Is this Kuudra's real lair?",
                "§e[NPC] §cElle§f: What madness is this—are we only now in the true abyss?");

        put("[NPC] Elle: ....",
                "§e[NPC] §cElle§f: ……… emptiness ………");

        put("[NPC] Elle: I'm not going down there... that's all you.",
                "§e[NPC] §cElle§f: I shall not descend into that pit… that burden is yours alone.");
    }});

    public static final Map<String, String> SHAKESPEARE = unmodifiable(new HashMap<String, String>() {{

        // ── Intro / Fishing setup ───────────────────────────────────────────────────
        put("[NPC] Elle: Talk with me to begin!",
                "§e[NPC] §cElle§f: Hark! Come hither and speak, that our quest mayst commence anon!");

        put("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!",
                "§e[NPC] §cElle§f: Lo, brave souls, I shall cast mine hook and summon forth dread Kuudra!");

        put("[NPC] Elle: Head over to the main platform, I will join you when I get a bite!",
                "§e[NPC] §cElle§f: Repair ye unto yon platform—anon shall I join, when fortune grants a bite!");

        // ── Hooking Kuudra ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Nope, that's just a Magmafish!",
                "§e[NPC] §cElle§f: Alas, ’tis but a humble Magmafish, no beast of legend.");

        put("[NPC] Elle: Wait... I think I've hooked onto something!",
                "§e[NPC] §cElle§f: Soft! Methinks mine line hath caught some mighty thing!");

        put("[NPC] Elle: Whatever it is, it sure is big! This might be it!",
                "§e[NPC] §cElle§f: By my troth, this creature is vast! Perchance the very foe we seek!");

        put("[NPC] Elle: Uh oh.",
                "§e[NPC] §cElle§f: Marry, ill fortune befalls us…");

        put("[NPC] Elle: Not again!",
                "§e[NPC] §cElle§f: Not again, by Saint George!");

        // ── Supplies phase ──────────────────────────────────────────────────────────
        put("[NPC] Elle: Phew... that was close!",
                "§e[NPC] §cElle§f: Prithee, that was a narrow escape indeed!");

        put("[NPC] Elle: ARGH! All of the supplies fell into the lava! You need to retrieve them quickly!",
                "§e[NPC] §cElle§f: Alas! Our victuals are lost to the molten deep—§chasten thee and recover them swiftly§f!");

        put("[NPC] Elle: Use your Lava Fishing Rods to pull them closer to the platform and collect them!",
                "§e[NPC] §cElle§f: Wield thy §cLava Rods§f to §cdraw forth the crates§f unto this deck!");

        put("[NPC] Elle: I'll help you clear out Kuudra's minions, go fetch my supplies!",
                "§e[NPC] §cElle§f: I shall smite Kuudra’s vile thralls—§cthou must fetch my gear§f!");

        put("[NPC] Elle: OMG! Great work collecting my supplies!",
                "§e[NPC] §cElle§f: Well done, noble companions, thou hast gathered mine goods!");

        put("[NPC] Elle: Now... please just cover me for a few minutes, I'll put together our special weapon!",
                "§e[NPC] §cElle§f: Stand guard but awhile, whilst I fashion our secret weapon of war!");

        put("[NPC] Elle: It's time to build the Ballista again! Cover me!",
                "§e[NPC] §cElle§f: Again must we craft the Ballista—§cprotect me whilst I labor§f!");

        // ── Horde / Immune phase ───────────────────────────────────────────────────
        put("[NPC] Elle: What the hell is that noise?! I think we've made him mad!",
                "§e[NPC] §cElle§f: What devilish din is this? Methinks we have stirred his wrath!");

        put("[NPC] Elle: Looks like the horde is coming! Cover me!",
                "§e[NPC] §cElle§f: Behold! A horde approacheth—§cstand firm and shield me§f!");

        put("[NPC] Elle: Damaging Kuudra directly is futile. His thick skin is impenetrable with conventional weapons!",
                "§e[NPC] §cElle§f: Strike him not directly—§chis hide withstands all mortal arms§f!");

        put("[NPC] Elle: I'm sure that'll buff out...",
                "§e[NPC] §cElle§f: Methinks ’twill mend… perchance…");

        put("[NPC] Elle: Argh, that didn't sound healthy.",
                "§e[NPC] §cElle§f: That sound bode ill, good sirs.");

        put("[NPC] Elle: BRUH",
                "§e[NPC] §cElle§f: Forsooth… BRUH.");

        put("[NPC] Elle: Guys!! You need to focus the tentacles!",
                "§e[NPC] §cElle§f: Attend ye! Smite the tentacles, and let not thy blows be scattered!");

        put("[NPC] Elle: That's a lot of damage!",
                "§e[NPC] §cElle§f: Lo! Such grievous harm dost thou deal!");

        // ── Ballista build / fuel ───────────────────────────────────────────────────
        put("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!",
                "§e[NPC] §cElle§f: At last, the Ballista is wrought—stout enough to withstand Kuudra’s mighty strikes!");

        put("[NPC] Elle: We need to find the fuel for the Ballista, it must have fallen in the lava with the rest of my supplies!",
                "§e[NPC] §cElle§f: Seek we fuel, for it hath plunged into the lava with my other wares!");

        put("[NPC] Elle: I'm sure that'll buff out... I hope..",
                "§e[NPC] §cElle§f: Perchance it shall mend… I pray it so.");

        // ── Re-emerge / Fire ballista ───────────────────────────────────────────────
        put("[NPC] Elle: Huh, what is that noise?",
                "§e[NPC] §cElle§f: What strange sound breaketh upon mine ear?");

        put("[NPC] Elle: Looks like Kuudra has re-emerged! It's time to shoot him with the Ballista!",
                "§e[NPC] §cElle§f: Kuudra riseth anew—§clet the Ballista sing forth§f!");

        put("[NPC] Elle: Whow.. WHOW! What's happening!!",
                "§e[NPC] §cElle§f: What sorcery is this?! What fate befalls us!");

        // ── Eaten / Belly (muffled) ────────────────────────────────────────────────
        put("Elle has been eaten by Kuudra!",
                "§cWoe! Elle is swallowed whole by Kuudra’s maw!");

        put("[NPC] Elle: *muffled* Help! I'm still alive in here! It stinks!",
                "§e[NPC] §cElle§f: §8*muffled*§f Aid me! I yet live, though foul vapors surround!");

        put("[NPC] Elle: *muffled*I think I'm in Kuudra's Belly! I was standing too close!",
                "§e[NPC] §cElle§f: §8*muffled*§f I am trapped within §cKuudra’s belly§f—stood o’ermuch near!");

        put("[NPC] Elle: *muffled* See if you can find a way to get in here too! I see something which may help us kill Kuudra!",
                "§e[NPC] §cElle§f: §8*muffled*§f Seek thou passage hither—I espy a thing to §cslay Kuudra§f!");

        put("[NPC] Elle: *muffled* Hurry!",
                "§e[NPC] §cElle§f: §8*muffled*§f Make haste!");

        // ── Inside belly gameplay ───────────────────────────────────────────────────
        put("[NPC] Elle: Oh, funny seeing you here.",
                "§e[NPC] §cElle§f: Verily, a strange place for reunion, good friend.");

        put("[NPC] Elle: What? Yes I am still alive, I landed inside Kuudra's belly here.",
                "§e[NPC] §cElle§f: Aye, I yet draw breath, for I fell within Kuudra’s very gut.");

        put("[NPC] Elle: It looks like if we mine these purple pods, we can do some real damage to Kuudra!",
                "§e[NPC] §cElle§f: Methinks to mine these §cpurple pods§f is to wound Kuudra most grievously!");

        put("[NPC] Elle: Oh come on! What is it this time!",
                "§e[NPC] §cElle§f: O heavens, what mischief comes anew?!");

        put("[NPC] Elle: I'll fight the mobs, you focus on destroying those pods!",
                "§e[NPC] §cElle§f: I shall battle these fiends—§cdo thou smite the pods§f!");

        // ── Submerge / Escape / Flush ───────────────────────────────────────────────
        put("[NPC] Elle: The lava level is rising inside Kuudra! He must have submerged under the lava!",
                "§e[NPC] §cElle§f: The molten tide doth rise—he hath plunged beneath the fire!");

        put("[NPC] Elle: We need to get out before we burn in this lava!",
                "§e[NPC] §cElle§f: We must flee ere we roast within this molten hell!");

        put("[NPC] Elle: We can escape back out his throat at the top of his stomach!",
                "§e[NPC] §cElle§f: Our way lieth yonder—§cup his throat, atop the belly§f!");

        put("[NPC] Elle: aaaaaaaaaaaaaaaaaaaaaaaaaaaah",
                "§e[NPC] §cElle§f: AAAAAAAAA—alas, we are undone!");

        put("[NPC] Elle: When Kuudra submerges, we will be flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: When he submergeth, we are §ccast out of his belly§f—beware, and re-enter with care!");

        put("[NPC] Elle: Kuudra submerged and I was flushed out of his stomach! We need to get back inside but be careful when he submerges!",
                "§e[NPC] §cElle§f: He plunged and I was §ccast forth§f—return we must, yet with caution!");

        put("[NPC] Elle: Kuudra has these purple pods in his stomach which look vulnerable! If we mine the pods we might be able to damage Kuudra!",
                "§e[NPC] §cElle§f: In his maw lie §cpurple pods§f—mine them, and Kuudra shall bleed!");

        // ── Stun / Ballista window ─────────────────────────────────────────────────
        put("[NPC] Elle: That looks like it hurt! Quickly, while Kuudra is distracted, shoot him with the Ballista!",
                "§e[NPC] §cElle§f: He reel’d from the blow! §cLoose the Ballista now§f, whilst he be stagger’d!");

        // ── Event callouts (adds/minions) ───────────────────────────────────────────
        put("[NPC] Elle: A group of Wandering Blazes are emerging from Kuudra's mouth!",
                "§e[NPC] §cElle§f: Behold, §cWandering Blazes§f spew from his maw—strike them down!");

        put("[NPC] Elle: Look out, Magma Followers! Don't let them merge!",
                "§e[NPC] §cElle§f: Beware the §cMagma Followers§f—suffer them not to join!");

        put("[NPC] Elle: A Dropship is approaching! Take it down before it's too late!",
                "§e[NPC] §cElle§f: §cDropship§f approacheth—bring it low ere doom befalls!");

        put("[NPC] Elle: A fleet of Dropships are approaching! Take them down before it's too late!",
                "§e[NPC] §cElle§f: A fleet of §cDropships§f dost approach—smite them from the heavens!");

        put("[NPC] Elle: Those Energized Chunks really pack a punch when they explode! Try hitting them towards Kuudra!",
                "§e[NPC] §cElle§f: These §cEnergized Chunks§f do burst with might—send them ’gainst Kuudra!");

        // ── Ends / Phases ───────────────────────────────────────────────────────────
        put("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!",
                "§e[NPC] §cElle§f: Pow! Surely this ends him—he hath no more within!");

        put("[NPC] Elle: I knew you could do it!",
                "§e[NPC] §cElle§f: Verily, I knew thou couldst prevail!");

        put("[NPC] Elle: One last flail before admitting defeat, huh?",
                "§e[NPC] §cElle§f: One last thrash ere he yieldeth to fate, is’t not so?");

        put("[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!",
                "§e[NPC] §cElle§f: Well fought, all! The strife is ended—come, let us quit ere more peril arise!");

        put("[NPC] Elle: What's happening? Why isn't he dying?",
                "§e[NPC] §cElle§f: What strange enchantment? Why doth he not perish?");

        put("[NPC] Elle: What just happened!? Is this Kuudra's real lair?",
                "§e[NPC] §cElle§f: What wonder? Be this Kuudra’s true lair, unveiled at last?");

        put("[NPC] Elle: ....",
                "§e[NPC] §cElle§f: ………");

        put("[NPC] Elle: I'm not going down there... that's all you.",
                "§e[NPC] §cElle§f: I shall not descend thither… that burden is thine alone.");
    }});

    private static Map<String, String> unmodifiable(Map<String, String> m) {
        return java.util.Collections.unmodifiableMap(m);
    }
}
