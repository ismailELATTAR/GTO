---
name: frontend-design
description: Design philosophy and process for building distinctive, opinionated UI for PokerGTO — landing/marketing pages, dashboards, lesson/exercise screens, and poker table visuals. Use whenever asked to design or restyle a page, component, or visual flow, not just wire it up functionally. Pairs with angular-frontend-reviewer (code conventions) — this skill is about visual/UX quality.
---

# Frontend Design

Approach this as the design lead at a small studio known for giving every client a visual identity that could not be mistaken for anyone else's. This client has already rejected proposals that felt templated, and is paying for a distinctive point of view: make deliberate, opinionated choices about palette, typography, and layout that are specific to this brief, and take one real aesthetic risk you can justify.

## Ground it in the subject

If the brief does not pin down what the product or subject is, pin it yourself before designing: name one concrete subject, its audience, and the page's single job, and state your choice. If there's any information in memory about the human's preferences, context about what they're building, or designs made before, use that as a hint. The subject's own world — its materials, instruments, artifacts, and vernacular — is where distinctive choices come from. Build with the brief's real content and subject matter throughout.

For this project specifically: the subject is No-Limit Hold'em theory and training — felt, chips, cards, ranges, solver output, the texture of a real table and the discipline of study, not generic "gamified app" or "fintech dashboard" visual language. A poker player opening this app should feel it's made by people who actually play, not by a generic ed-tech template.

## Design principles

For web designs, the hero is a thesis. Open with the most characteristic thing in the subject's world, in whatever form makes sense for it: a headline, an image, an animation, a live demo, an interactive moment. Be deliberate with your choice: a big number with a small label, supporting stats, and a gradient accent is the template answer — only use it if that's truly the best option.

Typography carries the personality of the page. Pair the display and body faces deliberately, not the same families reached for on any other project, and set a clear type scale with intentional weights, widths, and spacing. Make the type treatment itself a memorable part of the design, not a neutral delivery vehicle for the content.

Structure is information. Structural devices — numbering, eyebrows, dividers, labels — should encode something true about the content, not decorate it. Numbered markers (01 / 02 / 03) are only appropriate if the content actually is a sequence, like a real process or a typed timeline where order carries information the reader needs. Question whether a device like this actually makes sense before incorporating it.

Leverage motion deliberately. Think about where, and if, animation can serve the subject: a page-load sequence, a scroll-triggered reveal, hover micro-interactions, ambient atmosphere. An orchestrated moment usually lands harder than scattered effects; choose what the direction calls for. Sometimes less is more — extra animation contributes to the feeling that a design is AI-generated.

Match complexity to the vision. Maximalist directions need elaborate execution; minimal directions need precision in spacing, type, and detail. Elegance is executing the chosen vision well.

Consider written content carefully. Often a brief has no real content, and it's up to you to write copy. Copy can make a design feel as templated as the layout itself — see "More on writing" below.

## Process: brainstorm, explore, plan, critique, build, critique again

For calibration: AI-generated design right now clusters around three looks: (1) a warm cream background (near `#F4F1EA`) with a high-contrast serif display and a terracotta accent; (2) a near-black background with a single bright acid-green or vermilion accent; (3) a broadsheet-style layout with hairline rules, zero border-radius, and dense newspaper-like columns. All three are legitimate for some briefs, but they are defaults rather than choices, and they appear regardless of subject. Where the brief pins down a visual direction, follow it exactly — the brief's own words always win, including when it asks for one of these looks. Where it leaves an axis free, don't spend that freedom on one of these defaults.

Work in two passes. First, brainstorm a short design plan: a compact token system with color, type, layout, and signature.
- **Color**: describe the palette as 4–6 named hex values.
- **Type**: typefaces for 2+ roles — a characterful display face used with restraint, a complementary body face, and a utility face for captions/data if needed.
- **Layout**: a layout concept, using one-sentence prose descriptions and ASCII wireframes to ideate and compare.
- **Signature**: the single unique element this page will be remembered by, one that embodies the brief appropriately.

Then review that plan against the brief before building: if any part reads like the generic default for any similar page (work through a similar prompt mentally to see if it lands somewhere similar) rather than a choice made for this specific brief, revise it and say what changed and why. Only after confirming the plan's relative uniqueness should code get written — follow the revised plan exactly, deriving every color and type decision from it.

When writing the CSS/Tailwind, watch selector/utility specificity conflicts — it's easy to end up with rules that cancel each other out, especially around spacing between sections.

Do most of this planning and iteration in thinking; only show ideas once there's real confidence they'll land well.

## Restraint and self-critique

Spend boldness in one place. Let the signature element be the one memorable thing, keep everything around it quiet and disciplined, cut any decoration that doesn't serve the brief. Not taking a risk can be a risk itself. Build to a quality floor without announcing it: responsive down to mobile (this app is mobile-first per its own README — take that literally, design mobile layouts first, not as an afterthought breakpoint), visible keyboard focus, reduced motion respected. Critique the work as it's built, taking screenshots where the environment supports it. Consider Chanel's advice: before leaving the house, take a look in the mirror and remove one accessory.

## More on writing in design

Words appear in a design for one reason: to make it easier to understand, and therefore easier to use. They are design material, not decoration. Bring the same intentionality to copy as to spacing and color. Before writing anything, ask what the design needs to say, and how best to say it to help someone navigate the experience.

Write from the end user's side of the screen. Name things by what people control and recognize, never by how the system is built — a player tracks their progress, not their "XP transaction log." Describe what something does in plain terms rather than selling it.

Use active voice as default. A control says exactly what happens when used: "Save changes," not "Submit." An action keeps its name through the whole flow — the button that says "Submit answer" produces feedback that refers to "your answer," not "the response."

Treat failure and emptiness as moments for direction, not mood. Explain what went wrong and how to fix it, in the interface's voice. Errors don't apologize and are never vague. An empty screen (no exercises yet for a topic, no attempts logged) is an invitation to act, not a dead end.

Keep the register conversational and tuned: plain verbs, sentence case, no filler, tone matched to the audience — people studying poker theory seriously, not casual mobile-game players. Let each element do exactly one job.

## Applying this within PokerGTO's actual stack

- Angular 22 standalone components, Tailwind CSS, mobile-first (per README/ARCHITECTURE.md) — design within that stack, don't propose a different one.
- `shared/poker/*` (card, chip-stack, range-matrix, table) are the existing presentational building blocks — a new design should extend or restyle these consistently rather than inventing parallel one-off poker visuals per feature. If a design calls for a new shared visual primitive (e.g. a new chip/card treatment), build it as a reusable `shared/poker/*` or `shared/ui/*` component, not inline in one feature.
- Content-as-data: any copy/labels that are actual poker curriculum content (exercise text, range labels) come from the backend DTOs, not hardcoded in the component — see `angular-frontend-reviewer` for that boundary. UI chrome copy (button labels, empty states, error messages) is fine to write directly per the writing guidance above.
- Run `angular-frontend-reviewer` after implementing to check the code lands within this project's conventions (standalone, signals, no hardcoded content) — this skill governs the visual/UX judgment call, that subagent governs the code hygiene.
